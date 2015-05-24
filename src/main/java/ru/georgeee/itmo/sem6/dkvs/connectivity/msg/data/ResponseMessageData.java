package ru.georgeee.itmo.sem6.dkvs.connectivity.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.OpResult;

import java.util.ArrayList;
import java.util.List;

public class ResponseMessageData extends AbstractMessageData {
    @Getter
    private final int commandId;
    @Getter
    private final OpResult opResult;

    public ResponseMessageData(int commandId, OpResult opResult) {
        this.commandId = commandId;
        this.opResult = opResult;
    }

    public ResponseMessageData(Message parent) throws MessageParsingException {
        try {
            this.commandId = Integer.parseInt(parent.getArgs()[0]);
            this.opResult = OpResult.parseFromArgs(parent.getArgs(), 1);
        } catch (NumberFormatException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.RESPONSE;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, commandId);
        appendToArgs(args, opResult);
        return args;
    }

}
