package ru.georgeee.itmo.sem6.dkvs.connectivity.msg;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class Command implements ArgsAppendable {
    @Getter
    private final UUID clientId;

    @Getter
    private final int commandId;

    @Getter
    private final Op op;

    public Command(UUID clientId, int commandId, Op op) {
        this.clientId = clientId;
        this.commandId = commandId;
        this.op = op;
    }

    public static UUID generateClientId() {
        return UUID.randomUUID();
    }

    public static Command parseFromArgs(String[] args, int i) throws MessageParsingException {
        try {
            UUID clientId = UUID.fromString(args[i]);
            int commandId = Integer.parseInt(args[i + 1]);
            Op op = Op.parseFromArgs(args, i + 2);
            return new Command(clientId, commandId, op);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new MessageParsingException(args, e);
        }
    }

    @Override
    public void appendToArgs(List<String> args) {
        args.add(clientId.toString());
        args.add(String.valueOf(commandId));
        op.appendToArgs(args);
    }

}
