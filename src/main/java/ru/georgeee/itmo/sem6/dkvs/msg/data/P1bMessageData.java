package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.*;

import java.util.HashSet;
import java.util.Set;

public class P1bMessageData extends AbstractMessageData {
    @Getter
    @ArgsField
    private final String acceptorId;
    @Getter
    @ArgsField
    private final BallotNumber ballotNumber;
    @Getter @ArgsField
    private final int scoutId;

    @Getter
    @ArgsField
    @ArgsCollectionField(element = PValue.class, container = HashSet.class)
    private final Set<PValue> accepted;

    @ArgsConstructor
    public P1bMessageData(String acceptorId, BallotNumber ballotNumber, int scoutId, Set<PValue> accepted) {
        this.acceptorId = acceptorId;
        this.ballotNumber = ballotNumber;
        this.scoutId = scoutId;
        this.accepted = accepted;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.P1B;
    }

}
