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
    private final Map<Integer, Command> proposals;
    private int nextCommanderId;
    final Map<Integer, Commander> commanders;
    final Map<Integer, Scout> scouts;
    private final Map<Integer, Command> decisionsCache;
    private final List<Destination> replicas;
    final List<Destination> acceptors;

    public Leader(AbstractController controller) {
        super(controller);
        SystemConfiguration sysConfiguration = controller.getConnectionManager().getSystemConfiguration();
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
                spawnScout(ballotNumber);
            }
        });
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
            if (!proposals.containsKey(slotId)) {
                proposals.put(slotId, command);
                if (active) {
                    spawnCommander(new PValue(ballotNumber, slotId, command));
                }
            }
        }
    }

    private void process(P1bMessageData msg) {
        Scout scout = scouts.get(msg.getBallotNumber().getBallotId());
        if (scout != null) {
            scout.consume(msg);
        }
    }

    void reportPreempted(BallotNumber b2) {
        if (b2.compareTo(ballotNumber) > 0) {
            active = false;
            ballotNumber = new BallotNumber(b2.getBallotId() + 1, getSelfId());
            spawnScout(ballotNumber);
        }
    }

    void reportAdopted(BallotNumber b, Set<PValue> pValues) {
        proposals.putAll(getPMax(pValues));
        for (Map.Entry<Integer, Command> entry : proposals.entrySet()) {
            spawnCommander(new PValue(ballotNumber, entry.getKey(), entry.getValue()));
        }
        active = true;
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

    private void spawnScout(BallotNumber ballotNumber) {
        if (scouts.containsKey(ballotNumber.getBallotId())) {
            log.error("Scout for ballot {} is already spawned", ballotNumber);
        } else {
            Scout scout = new Scout(this, ballotNumber);
            scouts.put(ballotNumber.getBallotId(), scout);
            scout.init();
        }
    }

    void registerDecision(int slotId, Command command) {
        Message decisionMessage = new DecisionMessageData(slotId, command).createMessage();
        for (Destination replica : replicas) {
            send(decisionMessage, replica);
        }
        decisionsCache.put(slotId, command);
    }


}
