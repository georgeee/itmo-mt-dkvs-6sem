package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.StateHolder;
import ru.georgeee.itmo.sem6.dkvs.StateHolderImpl;
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
    private final boolean proposeLocalOnly;
    private final StateHolder stateHolder;
    private final Queue<Command> commandQueue;
    private final Map<Command, CommandState> commandStates;
    private final Map<Command, OpResult> commandResults;
    private final List<Destination> leaders;
    private final Map<Integer, Command> decisions;
    private final Map<Integer, Command> proposals;
    //How much slots we can keep undecided
    private final int slotWindow;
    //
    private int slotIn;
    private int slotOut;

    public Replica(ServerController controller) {
        super(controller);
        SystemConfiguration configuration = controller.getSystemConfiguration();
        commandQueue = new ArrayDeque<>();
        leaders = configuration.getDestinations(Role.LEADER);
        slotWindow = configuration.getPaxosSlotWindow();
        proposals = new HashMap<>();
        decisions = new HashMap<>();
        commandStates = new HashMap<>();
        stateHolder = new StateHolderImpl();
        commandResults = new HashMap<>();
        proposeLocalOnly = configuration.isProposeLocalOnly() && controller.has(Role.LEADER);
    }

    private void propose() {
        while ((slotWindow <= 0 || slotIn < slotOut + slotWindow) && !commandQueue.isEmpty()) {
            if (decisions.get(slotIn) == null) {
                Command command = commandQueue.poll();
                final Message message = new ProposeMessageData(slotIn, command, getSelfId()).createMessage();
                proposals.put(slotIn, command);
                setState(command, CommandState.PROPOSED);
                addForExecution(new Runnable() {
                    @Override
                    public void run() {
                        sendProposals(message, false);
                    }
                });
                executeRepeating(new IsDecidedPredicate(slotIn), new Runnable() {
                    @Override
                    public void run() {
                        sendProposals(message, true);
                    }
                }, "waiting for decision for slot " + slotIn, false);
            }
            ++slotIn;
        }
    }

    private void sendProposals(Message message, boolean explicitSendAll) {
        if (proposeLocalOnly && !explicitSendAll) {
            send(message, getSelfDestination());
        } else {
            for (Destination leader : leaders) {
                send(message, leader);
            }
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
                    process(message.getAs(RequestMessageData.class));
                    break;
                case DECISION:
                    process(message.getAs(DecisionMessageData.class));
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
        Command prevDecision = decisions.get(slotId);
        if (prevDecision != null && !prevDecision.equals(command)) {
            log.error("Two distinct decisions for slot {}: {}, {}", slotId, prevDecision, command);
        }
        decisions.put(slotId, command);
        while (decisions.get(slotOut) != null) {
            Command decision = decisions.get(slotOut);
            Command proposal = proposals.get(slotOut);
            proposals.put(slotOut, null);
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
