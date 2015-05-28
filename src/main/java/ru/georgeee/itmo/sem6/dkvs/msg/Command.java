package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;

public class Command implements ArgsConvertible {
    @Getter @ArgsField
    private final String clientId;

    @Getter @ArgsField
    private final int commandId;

    @Getter @ArgsField
    private final Op op;

    @ArgsConstructor
    public Command(String clientId, int commandId, Op op) {
        this.clientId = clientId;
        this.commandId = commandId;
        this.op = op;
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
        return "{" +
                "clientId='" + clientId + '\'' +
                ", commandId=" + commandId +
                ", op=" + op +
                '}';
    }

}
