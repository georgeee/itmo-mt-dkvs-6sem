package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class PValue implements ArgsAppendable {
    @Getter
    private final int ballotId;
    @Getter
    private final int slotId;
    @Getter
    private final Command command;

    public PValue(int ballotId, int slotId, Command command) {
        this.ballotId = ballotId;
        this.slotId = slotId;
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PValue pValue = (PValue) o;

        if (ballotId != pValue.ballotId) return false;
        if (slotId != pValue.slotId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ballotId;
        result = 31 * result + slotId;
        return result;
    }

    public static Pair<PValue, Integer> parseFromArgs(String[] args, int i) throws MessageParsingException {
        try {
            int ballotId = Integer.parseInt(args[i++]);
            int slotId = Integer.parseInt(args[i++]);
            Pair<Command, Integer> commandPair = Command.parseFromArgs(args, i);
            Command command = commandPair.getLeft();
            i = commandPair.getRight();
            return new ImmutablePair<>(new PValue(ballotId, slotId, command), i);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new MessageParsingException("Error parsing from args: " + Arrays.toString(args), e);
        }
    }

    @Override
    public void appendToArgs(List<String> args) {
        args.add(String.valueOf(ballotId));
        args.add(String.valueOf(slotId));
        command.appendToArgs(args);
    }
}
