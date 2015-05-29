package ru.georgeee.itmo.sem6.dkvs.controller;

import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.NodeConfiguration;
import ru.georgeee.itmo.sem6.dkvs.config.Role;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.utils.Utils;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import static ru.georgeee.itmo.sem6.dkvs.msg.Message.Type.*;

public class ServerController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ServerController.class);
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
            Acceptor newAcceptor;
            try {
                newAcceptor = new DiskLoggingAcceptor(this);
            } catch (FileNotFoundException e) {
                log.warn("Error initializing disk acceptor; launching default", e);
                newAcceptor = new Acceptor(this);
            }
            this.acceptor = newAcceptor;
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

    public boolean has(Role role) {
        return nodeConfiguration.getRoles().contains(role);
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
