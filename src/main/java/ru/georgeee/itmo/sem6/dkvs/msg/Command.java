package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Command implements ArgsAppendable {
    @Getter
    private final String clientId;

    @Getter
    private final int commandId;

    @Getter
    private final Op op;

    public Command(String clientId, int commandId, Op op) {
        this.clientId = clientId;
        this.commandId = commandId;
        this.op = op;
    }

    public static Pair<Command, Integer> parseFromArgs(String[] args, int i) throws MessageParsingException {
        try {
            String clientId = args[i++];
            int commandId = Integer.parseInt(args[i++]);
            Pair<Op, Integer> opPair = Op.parseFromArgs(args, i);
            Op op = opPair.getLeft();
            i = opPair.getRight();
            return new ImmutablePair<>(new Command(clientId, commandId, op), i);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new MessageParsingException(args, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command = (Command) o;

        if (commandId != command.commandId) return false;
        if (!clientId.equals(command.clientId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientId.hashCode();
        result = 31 * result + commandId;
        return result;
    }

    @Override
    public String toString() {
        return "Command{" +
                "clientId='" + clientId + '\'' +
                ", commandId=" + commandId +
                ", op=" + op +
                '}';
    }

    @Override
    public void appendToArgs(List<String> args) {
        args.add(clientId);
        args.add(String.valueOf(commandId));
        op.appendToArgs(args);
    }

}
