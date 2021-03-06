package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConstructor;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

public class PingMessageData extends AbstractMessageData {
    @Getter
    @ArgsField
    private final Destination sender;
    @Getter @ArgsField
    private final String token;

    @ArgsConstructor
    public PingMessageData(Destination sender, String token) {
        this.sender = sender;
        this.token = token;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.PING;
    }

}
