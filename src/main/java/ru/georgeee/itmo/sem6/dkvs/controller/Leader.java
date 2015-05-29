package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.Role;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.*;
import ru.georgeee.itmo.sem6.dkvs.msg.data.DecisionMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P1bMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P2bMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.ProposeMessageData;

import java.util.*;

class Leader extends AbstractInstance {
    private static final Logger log = LoggerFactory.getLogger(Leader.class);
    private BallotNumber ballotNumber;
    private boolean active;
    //Slot id -> command
    private final Map<Integer, Command> proposals;
    private int nextCommanderId;
    private int nextScoutId;
    private final Map<Integer, Commander> commanders;
    private final Map<Integer, Scout> scouts;

    //Making this field volatile, cause it's observed by PingTask from other thread
    volatile Destination mainLeader;
    private int mainLeaderBallotId;

    private final Map<Integer, Command> decisionsCache;
    private final List<Destination> replicas;
    final List<Destination> acceptors;

    public Leader(AbstractController controller) {
        super(controller);
        SystemConfiguration sysConfiguration = controller.getSystemConfiguration();
        ballotNumber = new BallotNumber(0, getSelfId());
        commanders = new HashMap<>();
        scouts = new HashMap<>();
        proposals = new HashMap<>();
        replicas = sysConfiguration.getDestinations(Role.REPLICA);
        acceptors = sysConfiguration.getDestinations(Role.ACCEPTOR);
        decisionsCache = new HashMap<>();
        addForExecution(new Runnable() {
            @Override
            public void run() {
                spawnScout();
            }
        });
        addTimerTask(new LeaderPingTask(new Runnable() {
            @Override
            public void run() {
                gainControl(mainLeaderBallotId);
            }
        }, this, controller), sysConfiguration.getLeaderPingTimeout());
    }

    @Override
    protected void consumeImpl(Message message) {
        try {
            switch (message.getType()) {
                case P1B:
                    process(message.getAs(P1bMessageData.class));
                    break;
                case P2B:
                    process(message.getAs(P2bMessageData.class));
                    break;
                case PROPOSE:
                    process(message.getAs(ProposeMessageData.class));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type " + message.getType());
            }
        } catch (MessageParsingException e) {
            log.warn("Wrong message received", e);
        }
    }

    private void process(ProposeMessageData msg) {
        int slotId = msg.getSlotId();
        Command command = msg.getCommand();
        if (decisionsCache.containsKey(msg.getSlotId())) {
            //Some optimization
            Message message = new DecisionMessageData(slotId, decisionsCache.get(slotId)).createMessage();
            sendToNode(message, msg.getReplicaId());
        } else {
            Destination mainLeader = this.mainLeader;
            if (mainLeader != null) {
                send(msg.createMessage(), mainLeader);
            }
            if (!proposals.containsKey(slotId)) {
                proposals.put(slotId, command);
                if (active) {
                    spawnCommander(new PValue(ballotNumber, slotId, command));
                }
            }
        }
    }

    private void process(P1bMessageData msg) {
        Scout scout = scouts.get(msg.getScoutId());
        if (scout != null) {
            scout.consume(msg);
        }
    }

    private void gainControl(int prevBallotId) {
        ballotNumber = new BallotNumber(prevBallotId + 1, getSelfId());
        spawnScout();
    }

    private void passControl(BallotNumber b2) {
        this.mainLeaderBallotId = b2.getBallotId();
        this.mainLeader = new Destination(Destination.Type.NODE, b2.getLeaderId());
    }

    void reportPreempted(BallotNumber b2) {
        log.info("PREEMPTED ballotNumber={} (current ballotNumber={})", b2, ballotNumber);
        int compareResult = b2.compareTo(ballotNumber);
        if (compareResult != 0) {
            active = false;
            if (compareResult > 0) {
                if (b2.getLeaderId().equals(getSelfId())) {
                    gainControl(b2.getBallotId());
                } else {
                    passControl(b2);
                }
            } else {
                gainControl(ballotNumber.getBallotId());
            }
        } else {
            log.error("Corrupt PREEMPTED: received {} == {}", b2, ballotNumber);
        }
    }

    void reportAdopted(BallotNumber b2, Set<PValue> pValues) {
        log.info("ADOPTED ballotNumber={} pValues={} (current ballotNumber={})", b2, pValues, ballotNumber);
        int compareResult = b2.compareTo(ballotNumber);
        if (compareResult > 0) {
            log.info("Ignoring ADOPTED: received {} > current {}", b2, ballotNumber);
        } else if (compareResult == 0) {
            proposals.putAll(getPMax(pValues));
            for (Map.Entry<Integer, Command> entry : proposals.entrySet()) {
                spawnCommander(new PValue(ballotNumber, entry.getKey(), entry.getValue()));
            }
            active = true;
        } else {
            log.error("Corrupt ADOPTED: received {} < current {}", b2, ballotNumber);
        }
    }

    private Map<Integer, Command> getPMax(Set<PValue> pValues) {
        List<PValue> pValuesList = new ArrayList<>(pValues);
        Collections.sort(pValuesList, new Comparator<PValue>() {
            @Override
            public int compare(PValue o1, PValue o2) {
                return -o1.getBallotNumber().compareTo(o2.getBallotNumber());
            }
        });
        Map<Integer, Command> result = new HashMap<>();
        for (PValue pValue : pValuesList) {
            if (!result.containsKey(pValue.getSlotId())) {
                result.put(pValue.getSlotId(), pValue.getCommand());
            }
        }
        return result;
    }

    private void process(P2bMessageData msg) {
        int commanderId = msg.getCommanderId();
        Commander commander = commanders.get(commanderId);
        if (commander != null) {
            commander.consume(msg);
        }
    }

    private void spawnCommander(PValue pValue) {
        int commanderId = nextCommanderId++;
        Commander commander = new Commander(this, commanderId, pValue);
        commanders.put(commanderId, commander);
        commander.init();
    }

    /**
     * Spawns a scout for current ballot number
     * Method is called only in two places, right after new ballot is created
     */
    private void spawnScout() {
        int scoutId = nextScoutId++;
        Scout scout = new Scout(this, scoutId, ballotNumber);
        scouts.put(scoutId, scout);
        scout.init();
    }

    void registerDecision(int slotId, Command command) {
        log.info("Decided for slot {}, command: {}", slotId, command);
        Message decisionMessage = new DecisionMessageData(slotId, command).createMessage();
        for (Destination replica : replicas) {
            send(decisionMessage, replica);
        }
        decisionsCache.put(slotId, command);
    }


    public void unregister(Commander commander) {
        commanders.remove(commander.getCommanderId());
    }

    public void unregister(Scout scout) {
        scouts.remove(scout.getScoutId());
    }

}
