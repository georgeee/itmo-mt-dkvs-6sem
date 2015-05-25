package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

    public static Op createGetConsistentOperation(String key) {
        return new Op(Type.GET_CONSISTENT, key, null);
    }

    public static Op createSetOperation(String key, String value) {
        return new Op(Type.SET, key, value);
    }

    public static Op createDeleteOperation(String key) {
        return new Op(Type.DELETE, key, null);
    }


    public static Pair<Op, Integer> parseFromArgs(String[] args, int i) throws MessageParsingException {
        try {
            Type type = Type.valueOf(args[i++]);
            String key = null, value = null;
            switch (type) {
                case DELETE:
                case GET:
                case GET_CONSISTENT:
                case SET:
                    key = args[i++];
            }
            switch (type) {
                case SET:
                    value = args[i++];
            }
            return new ImmutablePair<>(new Op(type, key, value), i);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new MessageParsingException("Error parsing from args: " + Arrays.toString(args), e);
        }
    }

    @Override
    public String toString() {
        return "Op{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
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
        GET, SET, DELETE, GET_CONSISTENT
    }
}
