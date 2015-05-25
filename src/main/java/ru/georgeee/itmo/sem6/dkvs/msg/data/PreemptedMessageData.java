package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConstructor;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;
import ru.georgeee.itmo.sem6.dkvs.msg.BallotNumber;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

public class PreemptedMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final BallotNumber ballotNumber;

    @ArgsConstructor
    public PreemptedMessageData(BallotNumber ballotNumber) {
        this.ballotNumber = ballotNumber;
    }

    @ArgsConstructor
    @Override
    protected Message.Type getType() {
        return Message.Type.PREEMPTED;
    }

}
