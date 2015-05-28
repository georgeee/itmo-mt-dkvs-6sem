package ru.georgeee.itmo.sem6.dkvs.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.msg.BallotNumber;
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

    private BallotNumber ballotNumber;
    private final Set<PValue> accepted;

    public Acceptor(AbstractController controller) {
        super(controller);
        accepted = new HashSet<>();
        ballotNumber = new BallotNumber(-1, "<none>");
    }

    @Override
    protected void consumeImpl(Message message) {
        try {
            switch (message.getType()) {
                case P1A:
                    process(message.getAs(P1aMessageData.class));
                    break;
                case P2A:
                    process(message.getAs(P2aMessageData.class));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type " + message.getType());
            }
        } catch (MessageParsingException e) {
            log.warn("Wrong message received", e);
        }
    }

    private void process(P1aMessageData msg) {
        BallotNumber b = msg.getBallotNumber();
//        log.info("P1A received from leader {} with ballot {} (current ballot {})", msg.getLeaderId(), b, ballotNumber);
        if (ballotNumber == null || b.compareTo(ballotNumber) > 0) {
            ballotNumber = b;
        }
        //Executing non-repeating, cause we have no condition to check
        //Which is expected, assuming that acceptor is kind of memory
        Message message = new P1bMessageData(getSelfId(), ballotNumber, msg.getScoutId(), accepted).createMessage();
        sendToNode(message, msg.getLeaderId());
    }

    private void process(P2aMessageData p2aData) {
        PValue pValue = p2aData.getPValue();
        BallotNumber b = pValue.getBallotNumber();
        if (b.equals(ballotNumber)) {
            accepted.add(pValue);
        }
        //Executing non-repeating, cause we have no condition to check
        //Which is expected, assuming that acceptor is kind of memory
        Message message = new P2bMessageData(getSelfId(), p2aData.getCommanderId(), ballotNumber).createMessage();
        sendToNode(message, p2aData.getLeaderId());
    }


}
