package ru.georgeee.itmo.sem6.dkvs.connectivity.msg;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class Op implements ArgsAppendable {
    @Getter
    private final Type type;
    @Getter
    private final String key;
    @Getter
    private final String value;

    private Op(Type type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;

    }

    public static Op createGetOperation(String key) {
        return new Op(Type.GET, key, null);
    }

    public static Op createSetOperation(String key, String value) {
        return new Op(Type.SET, key, value);
    }

    public static Op createDeleteOperation(String key) {
        return new Op(Type.DELETE, key, null);
    }

    public static Op createPingOperation() {
        return new Op(Type.PING, null, null);
    }

    public static Op parseFromArgs(String[] args, int i) throws MessageParsingException {
        try {
            Type type = Type.valueOf(args[i]);
            String key = null, value = null;
            switch (type) {
                case DELETE:
                case GET:
                case SET:
                    key = args[i + 1];
            }
            switch (type) {
                case SET:
                    value = args[i + 2];
            }
            return new Op(type, key, value);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new MessageParsingException("Error parsing from args: " + Arrays.toString(args), e);
        }
    }

    @Override
    public void appendToArgs(List<String> args) {
        args.add(type.toString());
        if (key != null) {
            args.add(key);
            if (value != null) {
                args.add(value);
            }
        }
    }

    public enum Type {
        GET, SET, DELETE, PING
    }
}
