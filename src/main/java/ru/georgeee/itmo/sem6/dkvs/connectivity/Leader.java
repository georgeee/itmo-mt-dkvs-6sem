package ru.georgeee.itmo.sem6.dkvs.connectivity;

import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.Role;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.BallotNumber;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Leader extends AbstractInstance {
    private BallotNumber ballotNumber;
    private boolean active;
    private final List<PValue> proposals;
    private final Map<PValue, Commander> commanders;
    private final Map<Integer, Scout> scouts;
    private final List<Destination> replicas;
    private final List<Destination> acceptors;

    public Leader(Node node) {
        super(node);
        SystemConfiguration sysConfiguration = node.getConnectionManager().getSystemConfiguration();
        ballotNumber = new BallotNumber(0, getSelfId());
        commanders = new HashMap<>();
        scouts = new HashMap<>();
        proposals = new ArrayList<>();
        replicas = sysConfiguration.getDestinations(Role.REPLICA);
        acceptors = sysConfiguration.getDestinations(Role.ACCEPTOR);
    }

    @Override
    protected void consumeImpl(Message message) {

    }

    private class Commander{
//        private final PValue pValue;
//        private final Set<>


    }

    private class Scout {
    }
}
