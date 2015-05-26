package ru.georgeee.itmo.sem6.dkvs.connectivity;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.data.PingMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.PongMessageData;

class PingPong extends AbstractInstance {
    private static final Logger log = LoggerFactory.getLogger(PingPong.class);

    public PingPong(AbstractController controller) {
        super(controller);
    }

    @Override
    protected void consumeImpl(Message message) {
        try {
            switch (message.getType()) {
                case PING:
                    processPing(message.getAs(PingMessageData.class));
                    break;
                case PONG:
                    processPong(message.getAs(PongMessageData.class));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type " + message.getType());
            }
        } catch (MessageParsingException e) {
            log.warn("Wrong message received", e);
        }
    }

    private void processPong(PongMessageData msg) {
    }

    private void processPing(PingMessageData msg) {
    }


}
