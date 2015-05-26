package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.*;

public class P2aMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final String leaderId;
    @Getter @ArgsField
    private final int commanderId;
    @Getter @ArgsField
    private final PValue pValue;

    @ArgsConstructor
    public P2aMessageData(String leaderId, int commanderId, PValue pValue) {
        this.leaderId = leaderId;
        this.commanderId = commanderId;
        this.pValue = pValue;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.P2A;
    }

}
