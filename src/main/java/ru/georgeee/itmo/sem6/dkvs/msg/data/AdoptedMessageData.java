package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AdoptedMessageData extends AbstractMessageData {
    @Getter
    private final int ballotId;
    @Getter
    private final Set<PValue> pValues;

    public AdoptedMessageData(String leaderId, int ballotId, Set<PValue> pValues) {
        this.ballotId = ballotId;
        this.pValues = pValues;
    }

    public AdoptedMessageData(Message parent) throws MessageParsingException {
        try {
            this.ballotId = Integer.parseInt(parent.getArgs()[0]);
            this.pValues = parsePValuesFromArgs(parent.getArgs(), 1);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.ADOPTED;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, ballotId);
        for (PValue pValue : pValues) {
            appendToArgs(args, pValue);
        }
        return args;
    }

}
