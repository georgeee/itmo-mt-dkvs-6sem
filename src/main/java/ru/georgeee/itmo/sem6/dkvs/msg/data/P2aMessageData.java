package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;

import java.util.ArrayList;
import java.util.List;

public class P2aMessageData extends AbstractMessageData {
    @Getter
    private final String leaderId;
    @Getter
    private final PValue pValue;

    public P2aMessageData(String leaderId, PValue pValue) {
        this.leaderId = leaderId;
        this.pValue = pValue;
    }

    public P2aMessageData(Message parent) throws MessageParsingException {
        try {
            this.leaderId = parent.getArgs()[0];
            this.pValue = PValue.parseFromArgs(parent.getArgs(), 1).getLeft();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.P2A;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, leaderId);
        appendToArgs(args, pValue);
        return args;
    }

}
