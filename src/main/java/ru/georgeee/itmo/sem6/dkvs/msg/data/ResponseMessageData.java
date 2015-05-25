package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConstructor;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.OpResult;

public class ResponseMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final int commandId;
    @Getter @ArgsField
    private final OpResult opResult;

    @ArgsConstructor
    public ResponseMessageData(int commandId, OpResult opResult) {
        this.commandId = commandId;
        this.opResult = opResult;
    }
    @Override
    protected Message.Type getType() {
        return Message.Type.RESPONSE;
    }

}
