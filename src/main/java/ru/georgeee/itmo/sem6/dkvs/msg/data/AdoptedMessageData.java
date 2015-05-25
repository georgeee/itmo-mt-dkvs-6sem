package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.*;

import java.util.HashSet;
import java.util.Set;

public class AdoptedMessageData extends AbstractMessageData {
    @Getter
    @ArgsField
    private final BallotNumber ballotNumber;
    @Getter
    @ArgsField
    @ArgsCollectionField(element = PValue.class, container = HashSet.class)
    private final Set<PValue> pValues;

    @ArgsConstructor
    public AdoptedMessageData(BallotNumber ballotNumber, Set<PValue> pValues) {
        this.ballotNumber = ballotNumber;
        this.pValues = pValues;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.ADOPTED;
    }

}
