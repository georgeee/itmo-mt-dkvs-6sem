package ru.georgeee.itmo.sem6.dkvs.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.Role;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.*;
import ru.georgeee.itmo.sem6.dkvs.msg.data.DecisionMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.ProposeMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.RequestMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.ResponseMessageData;

import java.util.*;

/**
 * Class for replica role
 * All structures are intended to be used by a single thread
 */
class Replica extends AbstractInstance {
    private static final Logger log = LoggerFactory.getLogger(Replica.class);
    private final StateHolder stateHolder;
    private final Queue<Command> commandQueue;
    private final Map<Command, CommandState> commandStates;
    private final Map<Command, OpResult> commandResults;
    private final List<Destination> leaders;
    private final List<Command> decisions;
    private final List<Command> proposals;
    //How much slots we can keep undecided
    private final int slotWindow;
    private int slotIn;
    private int slotOut;

    public Replica(Node node) {
        super(node);
        SystemConfiguration configuration = node.getConnectionManager().getSystemConfiguration();
        commandQueue = new ArrayDeque<>();
        leaders = configuration.getDestinations(Role.LEADER);
        slotWindow = configuration.getPaxosSlotWindow();
        proposals = new ArrayList<>();
        decisions = new ArrayList<>();
        commandStates = new HashMap<>();
        stateHolder = new StateHolderImpl();
        commandResults = new HashMap<>();
        enlargeSlotSets(slotIn);
    }

    private void enlargeSlotSets(int slotIndex) {
        while (decisions.size() < slotIndex + 1) {
            decisions.add(null);
        }
        while (proposals.size() < slotIndex + 1) {
            proposals.add(null);
        }
    }

    private void send(Message message, Destination destination) {
        node.getConnectionManager().send(message, destination);
    }

    private void propose() {
        while ((slotWindow <= 0 || slotIn < slotOut + slotWindow) && !commandQueue.isEmpty()) {
            if (decisions.get(slotIn) == null) {
                final Command command = commandQueue.poll();
                proposals.set(slotIn, command);
                setState(command, CommandState.PROPOSED);
                executeRepeating(new IsDecidedPredicate(slotIn), new Runnable() {
                    @Override
                    public void run() {
                        for (Destination leader : leaders) {
                            send(new ProposeMessageData(slotIn, command).createMessage(), leader);
                        }
                    }
                });
            }
            ++slotIn;
            enlargeSlotSets(slotIn);
        }
    }

    private void performSlotOutDecision() {
        Command command = decisions.get(slotOut);
        if (command == null) {
            throw new IllegalStateException("Slot out command is null");
        }
        CommandState state = getState(command);
        if (state != CommandState.EXECUTED) {
            sendResponseToClient(command, executeCommand(command));
        }
        ++slotOut;
    }

    @Override
    protected void consumeImpl(Message message) {
        try {
            switch (message.getType()) {
                case REQUEST:
                    process(message.getRequestData());
                    break;
                case DECISION:
                    process(message.getDecisionData());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type " + message.getType());
            }
            propose();
        } catch (MessageParsingException e) {
            log.warn("Wrong message received", e);
        }
    }

    private void process(RequestMessageData requestData) {
        Command command = requestData.getCommand();
        CommandState commandState = getState(command);
        if (commandState == null) {
            Op op = command.getOp();
            if (op.getType() == Op.Type.GET) {
                //Execute immediately
                sendResponseToClient(command, executeCommand(command));
            } else {
                commandQueue.add(command);
                setState(command, CommandState.REQUESTED);
            }
        } else {
            switch (commandState) {
                case REQUESTED:
                case PROPOSED:
                case DECIDED:
                    //Do nothing, command is already being processed
                    break;
                case EXECUTED:
                    sendResponseToClient(command, getResult(command));
                    break;
            }
        }
    }

    private void process(DecisionMessageData decisionData) {
        Command command = decisionData.getCommand();
        int slotId = decisionData.getSlotId();
        enlargeSlotSets(slotId);
        Command prevDecision = decisions.get(slotId);
        if (prevDecision != null && !prevDecision.equals(command)) {
            log.error("Two distinct decisions for slot {}: {}, {}", slotId, prevDecision, command);
        }
        decisions.set(slotId, command);
        while (decisions.get(slotOut) != null) {
            Command decision = decisions.get(slotOut);
            Command proposal = proposals.get(slotOut);
            proposals.set(slotOut, null);
            if (proposal != null && !decision.equals(proposal)) {
                commandQueue.add(proposal);
                setState(command, CommandState.REQUESTED);
            }
            performSlotOutDecision();
        }
    }

    private OpResult executeCommand(Command command) {
        if (getState(command) == CommandState.EXECUTED) {
            return getResult(command);
        }
        OpResult result = stateHolder.applyOp(command.getOp());
        setState(command, CommandState.EXECUTED);
        setResult(command, result);
        return result;
    }

    private void sendResponseToClient(Command command, OpResult result) {
        Message message = new ResponseMessageData(command.getCommandId(), result).createMessage();
        Destination destination = new Destination(Destination.Type.CLIENT, command.getClientId());
        send(message, destination);
    }

    private CommandState getState(Command command) {
        return commandStates.get(command);
    }

    private void setResult(Command command, OpResult result) {
        commandResults.put(command, result);
    }

    private void setState(Command command, CommandState state) {
        commandStates.put(command, state);
    }

    private OpResult getResult(Command command) {
        return commandResults.get(command);
    }

    private class IsDecidedPredicate implements Predicate {
        private final int slotId;

        private IsDecidedPredicate(int slotId) {
            this.slotId = slotId;
        }

        @Override
        public boolean evaluate() {
            return decisions.get(slotId) != null;
        }
    }

    private enum CommandState {
        REQUESTED, PROPOSED, DECIDED, EXECUTED
    }
}
