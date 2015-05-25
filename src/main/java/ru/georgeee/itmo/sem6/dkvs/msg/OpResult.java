package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class OpResult implements ArgsAppendable {
    @Getter
    private final Type type;
    @Getter
    private final String key;
    @Getter
    private final String value;

    private OpResult(Type type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public static OpResult createValueResult(String key, String value) {
        return new OpResult(Type.VALUE, key, value);
    }

    public static OpResult createNotFoundResult() {
        return new OpResult(Type.NOT_FOUND, null, null);
    }

    public static OpResult createdStoredResult() {
        return new OpResult(Type.STORED, null, null);
    }

    public static OpResult createDeletedResult(String key, String value) {
        return new OpResult(Type.DELETED, key, value);
    }

    public static OpResult parseFromArgs(String[] args, int i) throws MessageParsingException {
        try {
            Type type = Type.valueOf(args[i++].toUpperCase());
            String key = null, value = null;
            switch (type) {
                case VALUE:
                case DELETED:
                    key = args[i++];
                    value = args[i++];
            }
            return new OpResult(type, key, value);
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
        VALUE, NOT_FOUND, STORED, DELETED
    }
}
