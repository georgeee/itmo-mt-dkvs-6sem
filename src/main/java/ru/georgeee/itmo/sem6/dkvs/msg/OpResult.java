package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class OpResult implements ArgsConvertibleExtended {
    @Getter
    @ArgsField
    private final Type type;
    @Getter
    @ArgsField
    private final String key;
    @Getter
    @ArgsField
    private final String value;

    @ArgsConstructor
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

    public static Pair<OpResult, Integer> parseFromArgs(String[] args, int i) throws MessageParsingException {
        try {
            Type type = Type.valueOf(args[i++].toUpperCase());
            String key = null, value = null;
            switch (type) {
                case VALUE:
                case DELETED:
                    key = args[i++];
                    value = args[i++];
            }
            return new ImmutablePair<>(new OpResult(type, key, value), i);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new MessageParsingException("Error parsing from args: " + Arrays.toString(args), e);
        }
    }

    @Override
    public void addToArgs(List<Object> args) {
        args.add(type);
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
