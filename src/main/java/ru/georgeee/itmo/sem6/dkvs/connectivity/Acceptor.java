package ru.georgeee.itmo.sem6.dkvs.connectivity;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P1aMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P1bMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P2aMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P2bMessageData;

import java.util.HashSet;
import java.util.Set;

class Acceptor extends AbstractInstance {
    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);

    private int ballotNumber;
    private final Set<PValue> accepted;

    public Acceptor(Node node) {
        super(node);
        accepted = new HashSet<>();
    }

    @Override
    protected void consumeImpl(Message message) {
        try {
            switch (message.getType()) {
                case P1A:
                    process(message.getP1aData());
                    break;
                case P2A:
                    process(message.getP2aData());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type " + message.getType());
            }
        } catch (MessageParsingException e) {
            log.warn("Wrong message received", e);
        }
    }

    private void process(P1aMessageData p1aData) {
        int b = p1aData.getBallotId();
        if (b > ballotNumber) {
            ballotNumber = b;
        }
        //Executing non-repeating, cause we have no condition to check
        //Which is expected, assuming that acceptor is kind of memory
        Message message = new P1bMessageData(getSelfId(), ballotNumber, accepted).createMessage();
        sendToNode(message, p1aData.getLeaderId());
    }

    private void process(P2aMessageData p2aData) {
        PValue pValue = p2aData.getPValue();
        int b = pValue.getBallotId();
        if (b == ballotNumber) {
            accepted.add(pValue);
        }
        //Executing non-repeating, cause we have no condition to check
        //Which is expected, assuming that acceptor is kind of memory
        Message message = new P2bMessageData(getSelfId(), ballotNumber).createMessage();
        sendToNode(message, p2aData.getLeaderId());
    }


}
