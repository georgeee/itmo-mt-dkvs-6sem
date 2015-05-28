package ru.georgeee.itmo.sem6.dkvs.controller;

import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.msg.BallotNumber;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P1aMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P1bMessageData;

import java.util.HashSet;
import java.util.Set;

class Scout extends MajorityWaiter implements Consumer<P1bMessageData> {
    private final Leader leader;
    private final int scoutId;
    private final BallotNumber b;
    private final Set<PValue> pValues;

    Scout(Leader leader, int scoutId, BallotNumber b) {
        super(leader.acceptors, leader);
        this.leader = leader;
        this.scoutId = scoutId;
        this.b = b;
        this.pValues = new HashSet<>();
    }


    @Override
    public void consume(P1bMessageData msg) {
        BallotNumber b2 = msg.getBallotNumber();
        if (b.equals(b2)) {
            pValues.addAll(msg.getAccepted());
            removeDestination(msg.getAcceptorId());
            if (isReceivedEnough()) {
                leader.reportAdopted(b, pValues);
                unregister();
            }
        } else {
            leader.reportPreempted(b2);
            unregister();
        }
    }

    private void unregister() {
        stop();
        leader.scouts.remove(b.getBallotId());
    }

    @Override
    protected Message getInitMessage() {
        return new P1aMessageData(leader.getSelfId(), b, scoutId).createMessage();
    }

    @Override
    protected String getName() {
        return "scout " + b;
    }
}
