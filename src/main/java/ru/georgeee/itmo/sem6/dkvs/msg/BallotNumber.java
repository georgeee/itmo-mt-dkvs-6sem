package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;

public class BallotNumber implements ArgsConvertible, Comparable<BallotNumber> {
    @Getter
    @ArgsField
    private final int ballotId;
    @Getter
    @ArgsField
    private final String leaderId;

    @ArgsConstructor
    public BallotNumber(int ballotId, String leaderId) {
        this.ballotId = ballotId;
        this.leaderId = leaderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BallotNumber that = (BallotNumber) o;

        if (ballotId != that.ballotId) return false;
        if (!leaderId.equals(that.leaderId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = leaderId.hashCode();
        result = 31 * result + ballotId;
        return result;
    }

    @Override
    public int compareTo(BallotNumber o) {
        return o.leaderId.equals(leaderId) ? Integer.valueOf(ballotId).compareTo(ballotId) : leaderId.compareTo(o.leaderId);
    }
}
