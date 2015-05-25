package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.*;

public class RequestMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final Command command;

    @ArgsConstructor
    public RequestMessageData(Command command) {
        this.command = command;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.REQUEST;
    }

}
