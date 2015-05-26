package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConstructor;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

public class PongMessageData extends AbstractMessageData {
    @Getter
    @ArgsField
    private final Destination sender;

    @ArgsConstructor
    public PongMessageData(Destination sender) {
        this.sender = sender;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.PONG;
    }

}
