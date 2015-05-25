package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;

import java.util.ArrayList;
import java.util.List;

public class PreemptedMessageData extends AbstractMessageData {
    @Getter
    private final int ballotId;

    public PreemptedMessageData(String leaderId, int ballotId) {
        this.ballotId = ballotId;
    }

    public PreemptedMessageData(Message parent) throws MessageParsingException {
        try {
            this.ballotId = Integer.parseInt(parent.getArgs()[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }


    @Override
    protected Message.Type getType() {
        return Message.Type.PREEMPTED;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, ballotId);
        return args;
    }

}
