package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class P1bMessageData extends AbstractMessageData {
    @Getter
    private final String acceptorId;
    @Getter
    private final int ballotId;
    @Getter
    private final Set<PValue> accepted;

    public P1bMessageData(String acceptorId, int ballotId, Set<PValue> accepted) {
        this.acceptorId = acceptorId;
        this.ballotId = ballotId;
        this.accepted = accepted;
    }

    public P1bMessageData(Message parent) throws MessageParsingException {
        try {
            this.acceptorId = parent.getArgs()[0];
            this.ballotId = Integer.parseInt(parent.getArgs()[1]);
            this.accepted = parsePValuesFromArgs(parent.getArgs(), 2);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.P1B;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, acceptorId);
        appendToArgs(args, ballotId);
        for (PValue pValue : accepted) {
            appendToArgs(args, pValue);
        }
        return args;
    }

}
