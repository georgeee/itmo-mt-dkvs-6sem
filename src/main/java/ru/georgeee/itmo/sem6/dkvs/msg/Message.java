package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.data.*;

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

    public P1aMessageData getP1aData() throws MessageParsingException {
        return new P1aMessageData(this);
    }

    public AdoptedMessageData getAdoptedData() throws MessageParsingException {
        return new AdoptedMessageData(this);
    }

    public PreemptedMessageData getPreemptedData() throws MessageParsingException {
        return new PreemptedMessageData(this);
    }

    public P2bMessageData getP2bData() throws MessageParsingException {
        return new P2bMessageData(this);
    }

    public P1bMessageData getP1bData() throws MessageParsingException {
        return new P1bMessageData(this);
    }

    public P2aMessageData getP2aData() throws MessageParsingException {
        return new P2aMessageData(this);
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
        P1A, P1B, P2A, P2B, ADOPTED, PREEMPTED,
        PING, PONG
    }
}
