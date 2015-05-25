package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConstructor;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;
import ru.georgeee.itmo.sem6.dkvs.msg.BallotNumber;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

public class P2bMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final String acceptorId;
    @Getter @ArgsField
    private final BallotNumber ballotNumber;

    @ArgsConstructor
    public P2bMessageData(String acceptorId, BallotNumber ballotNumber) {
        this.acceptorId = acceptorId;
        this.ballotNumber = ballotNumber;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.P2B;
    }

}
