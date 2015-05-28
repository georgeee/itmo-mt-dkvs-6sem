package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;

public class PValue implements ArgsConvertible {
    @Getter @ArgsField
    private final BallotNumber ballotNumber;
    @Getter @ArgsField
    private final int slotId;
    @Getter @ArgsField
    private final Command command;

    @ArgsConstructor
    public PValue(BallotNumber ballotNumber, int slotId, Command command) {
        this.ballotNumber = ballotNumber;
        this.slotId = slotId;
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PValue pValue = (PValue) o;

        if (slotId != pValue.slotId) return false;
        if (!ballotNumber.equals(pValue.ballotNumber)) return false;
        if (!command.equals(pValue.command)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "{" +
                "ballotNumber=" + ballotNumber +
                ", slotId=" + slotId +
                ", command=" + command +
                '}';
    }

    @Override
    public int hashCode() {
        int result = ballotNumber.hashCode();
        result = 31 * result + slotId;
        result = 31 * result + command.hashCode();
        return result;
    }

}
