package ru.georgeee.itmo.sem6.dkvs.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.Role;
import ru.georgeee.itmo.sem6.dkvs.msg.Command;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.Op;
import ru.georgeee.itmo.sem6.dkvs.msg.data.RequestMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.ResponseMessageData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Client extends AbstractInstance {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private final ClientController controller;
    private final Set<Integer> notResponded;
    private final List<Destination> leaders;

    public Client(ClientController controller) {
        super(controller);
        this.controller = controller;
        this.leaders = controller.getSystemConfiguration().getDestinations(Role.LEADER);
        this.notResponded = new HashSet<>();
    }

    @Override
    protected void consumeImpl(Message message) {
        try {
            switch (message.getType()) {
                case RESPONSE:
                    processResponse(message.getAs(ResponseMessageData.class));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type " + message.getType());
            }
        } catch (MessageParsingException e) {
            log.warn("Wrong message received", e);
        }
    }

    private void processResponse(ResponseMessageData msg) {
        notResponded.remove(msg.getCommandId());
        controller.processResponse(msg.getCommandId(), msg.getOpResult());
    }


    public void submitRequest(final int commandId, Op op) {
        final Message message = new RequestMessageData(new Command(getSelfId(), commandId, op)).createMessage();
        notResponded.add(commandId);
        executeRepeating(new Predicate() {
            @Override
            public boolean evaluate() {
                return !notResponded.contains(commandId);
            }
        }, new Runnable() {
            @Override
            public void run() {
                for (Destination leader : leaders) {
                    send(message, leader);
                }
            }
        }, "waiting for response on command #" + commandId);
    }
}
