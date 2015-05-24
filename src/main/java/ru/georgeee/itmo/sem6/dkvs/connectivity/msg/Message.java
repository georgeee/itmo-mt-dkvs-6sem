package ru.georgeee.itmo.sem6.dkvs.connectivity.msg;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.data.DecisionMessageData;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.data.ProposeMessageData;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.data.RequestMessageData;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.data.ResponseMessageData;

public class Message {

    @Getter
    private final Type type;

    @Getter
    private final String[] args;

    public Message(Type type, String[] args) {
        this.type = type;
        this.args = args;
    }

    public static Message parseMessage(String line) throws MessageParsingException {
        String[] fullArgs = line.trim().split("\\s+");
        String[] args = new String[fullArgs.length - 1];
        System.arraycopy(fullArgs, 1, args, 0, args.length);
        return new Message(parseType(fullArgs[0]), args);
    }

    private static Type parseType(String name) throws MessageParsingException {
        try {
            return Type.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new MessageParsingException("Wrong type " + name, e);
        }
    }

    public RequestMessageData getRequestData() throws MessageParsingException {
        return new RequestMessageData(this);
    }

    public ProposeMessageData getProposeData() throws MessageParsingException {
        return new ProposeMessageData(this);
    }

    public DecisionMessageData getDecisionData() throws MessageParsingException {
        return new DecisionMessageData(this);
    }

    public ResponseMessageData getResponseData() throws MessageParsingException {
        return new ResponseMessageData(this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(type.toString());
        for (String arg : args) {
            sb.append(' ').append(arg);
        }
        return sb.toString();
    }

    public enum Type {
        REQUEST, RESPONSE, DECISION, PROPOSE,
        P1A, P1B, P2A, P2B, ADOPTED, PREEMPTED
    }
}
