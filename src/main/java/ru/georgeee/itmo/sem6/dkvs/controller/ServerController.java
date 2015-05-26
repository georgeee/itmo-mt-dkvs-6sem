package ru.georgeee.itmo.sem6.dkvs.controller;

import lombok.AccessLevel;
import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.utils.Utils;
import ru.georgeee.itmo.sem6.dkvs.config.NodeConfiguration;
import ru.georgeee.itmo.sem6.dkvs.config.Role;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;

import java.util.List;
import java.util.Set;

import static ru.georgeee.itmo.sem6.dkvs.msg.Message.Type.*;

public class ServerController extends AbstractController {
    @Getter(AccessLevel.PACKAGE)
    private final NodeConfiguration nodeConfiguration;
    private final Acceptor acceptor;
    private final Leader leader;
    private final Replica replica;
    private ServerSocketListener serverSocketListener;

    public ServerController(SystemConfiguration systemConfiguration, NodeConfiguration nodeConfiguration) {
        super(systemConfiguration);
        this.nodeConfiguration = nodeConfiguration;
        Set<Role> roles = nodeConfiguration.getRoles();
        if (roles.contains(Role.ACCEPTOR)) {
            this.acceptor = new Acceptor(this);
            Utils.putBatch(consumers, acceptor, P1A, P2A);
        } else {
            this.acceptor = null;
        }
        if (roles.contains(Role.LEADER)) {
            this.leader = new Leader(this);
            Utils.putBatch(consumers, leader, P1B, P2B, PROPOSE);
        } else {
            this.leader = null;
        }
        if (roles.contains(Role.REPLICA)) {
            this.replica = new Replica(this);
            Utils.putBatch(consumers, replica, REQUEST, DECISION);
        } else {
            this.replica = null;
        }
    }

    @Override
    protected Destination.Type getDestinationType() {
        return Destination.Type.NODE;
    }

    @Override
    protected String getId() {
        return nodeConfiguration.getId();
    }

    @Override
    protected void createThreads(List<Thread> threads) {
        serverSocketListener = new ServerSocketListener(this);
        threads.add(new Thread(serverSocketListener));
        if (acceptor != null) {
            threads.add(new Thread(acceptor));
        }
        if (replica != null) {
            threads.add(new Thread(replica));
        }
        if (leader != null) {
            threads.add(new Thread(leader));
        }
    }

}
