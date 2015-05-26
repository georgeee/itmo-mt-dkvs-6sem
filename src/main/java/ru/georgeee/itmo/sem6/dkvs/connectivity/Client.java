package ru.georgeee.itmo.sem6.dkvs.connectivity;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.data.ResponseMessageData;
class Client extends AbstractInstance {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public Client(AbstractController controller) {
        super(controller);
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

    }


}
