package ru.georgeee.itmo.sem6.dkvs.controller;

import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.msg.BallotNumber;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P2aMessageData;
import ru.georgeee.itmo.sem6.dkvs.msg.data.P2bMessageData;

class Commander extends MajorityWaiter implements Consumer<P2bMessageData> {
    private final Leader leader;
    private final int commanderId;
    private final BallotNumber b;
    private final PValue pValue;


    Commander(Leader leader, int commanderId, PValue pValue) {
        super(leader.acceptors, leader);
        this.leader = leader;
        this.commanderId = commanderId;
        this.pValue = pValue;
        this.b = pValue.getBallotNumber();
    }


    @Override
    public void consume(P2bMessageData msg) {
        BallotNumber b2 = msg.getBallotNumber();
        if (b.equals(b2)) {
            removeDestination(msg.getAcceptorId());
            if (isReceivedEnough()) {
                leader.registerDecision(pValue.getSlotId(), pValue.getCommand());
                unregister();
            }
        } else {
            leader.reportPreempted(b2);
            unregister();
        }
    }

    private void unregister() {
        stop();
        leader.commanders.remove(commanderId);
    }


    @Override
    protected Message getInitMessage() {
        return new P2aMessageData(leader.getSelfId(), commanderId, pValue).createMessage();
    }

    @Override
    protected String getName() {
        return "commander " + commanderId;
    }
}
