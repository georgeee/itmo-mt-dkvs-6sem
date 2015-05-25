package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;

import java.util.ArrayList;
import java.util.List;

public class P2bMessageData extends AbstractMessageData {
    @Getter
    private final String acceptorId;
    @Getter
    private final int ballotId;

    public P2bMessageData(String acceptorId, int ballotId) {
        this.acceptorId = acceptorId;
        this.ballotId = ballotId;
    }

    public P2bMessageData(Message parent) throws MessageParsingException {
        try {
            this.acceptorId = parent.getArgs()[0];
            this.ballotId = Integer.parseInt(parent.getArgs()[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }


    @Override
    protected Message.Type getType() {
        return Message.Type.P2B;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, acceptorId);
        appendToArgs(args, ballotId);
        return args;
    }

}
