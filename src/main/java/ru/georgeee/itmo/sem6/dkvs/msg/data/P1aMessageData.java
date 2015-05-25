package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;

import java.util.ArrayList;
import java.util.List;

public class P1aMessageData extends AbstractMessageData {
    @Getter
    private final String leaderId;
    @Getter
    private final int ballotId;

    public P1aMessageData(String leaderId, int ballotId) {
        this.leaderId = leaderId;
        this.ballotId = ballotId;
    }


    public P1aMessageData(Message parent) throws MessageParsingException {
        try {
            this.leaderId = parent.getArgs()[0];
            this.ballotId = Integer.parseInt(parent.getArgs()[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.P1A;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, leaderId);
        appendToArgs(args, ballotId);
        return args;
    }

}
