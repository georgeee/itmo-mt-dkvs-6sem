package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.utils.Utils;

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
        String[] fullArgs = Utils.splitToArgs(line);
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

    public <T extends ArgsConvertible> T getAs(Class<T> clazz) throws MessageParsingException {
        return ArgsConverter.parse(clazz, this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(type.toString());
        for (String arg : args) {
            sb.append(' ').append(arg);
        }
        return sb.toString();
    }

    public enum Type {
        //Types from Paxos paper
        REQUEST, RESPONSE, DECISION, PROPOSE,
        P1A, P1B, P2A, P2B,
        //Custom types
        PING, PONG
    }
}
