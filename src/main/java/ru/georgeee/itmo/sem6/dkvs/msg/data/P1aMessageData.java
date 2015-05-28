package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConstructor;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;
import ru.georgeee.itmo.sem6.dkvs.msg.BallotNumber;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

public class P1aMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final String leaderId;
    @Getter @ArgsField
    private final BallotNumber ballotNumber;
    @Getter @ArgsField
    private final int scoutId;

    @ArgsConstructor
    public P1aMessageData(String leaderId, BallotNumber ballotNumber, int scoutId) {
        this.leaderId = leaderId;
        this.ballotNumber = ballotNumber;
        this.scoutId = scoutId;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.P1A;
    }

}
