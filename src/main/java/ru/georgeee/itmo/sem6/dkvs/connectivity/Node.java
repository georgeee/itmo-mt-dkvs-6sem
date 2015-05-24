package ru.georgeee.itmo.sem6.dkvs.connectivity;

import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.Utils;
import ru.georgeee.itmo.sem6.dkvs.config.NodeConfiguration;
import ru.georgeee.itmo.sem6.dkvs.config.Role;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.Message;

import java.util.*;

import static ru.georgeee.itmo.sem6.dkvs.connectivity.msg.Message.Type.*;

public class Node {
    private static final Logger log = LoggerFactory.getLogger(Node.class);
    @Getter(AccessLevel.PACKAGE)
    private final NodeConfiguration nodeConfiguration;
    @Getter(AccessLevel.PACKAGE)
    private final ConnectionManager connectionManager;
    private final Map<Message.Type, Consumer<Message>> consumers;
    private final Acceptor acceptor;
    private final Leader leader;
    private final Replica replica;
    private final List<Thread> threads;

    public Node(SystemConfiguration systemConfiguration, NodeConfiguration nodeConfiguration) {
        this.nodeConfiguration = nodeConfiguration;
        this.consumers = new EnumMap<>(Message.Type.class);
        Set<Role> roles = nodeConfiguration.getRoles();
        if (roles.contains(Role.ACCEPTOR)) {
            this.acceptor = new Acceptor(this);
            Utils.putBatch(consumers, acceptor, P1A, P2A);
        } else {
            this.acceptor = null;
        }
        if (roles.contains(Role.LEADER)) {
            this.leader = new Leader(this);
            Utils.putBatch(consumers, leader, P1B, P2B, PROPOSE, ADOPTED, PREEMPTED);
        } else {
            this.leader = null;
        }
        if (roles.contains(Role.REPLICA)) {
            this.replica = new Replica(this);
            Utils.putBatch(consumers, replica, REQUEST, DECISION);
        } else {
            this.replica = null;
        }
        threads = new ArrayList<>();
        this.connectionManager = new ConnectionManager(systemConfiguration, getSelfDestination(), new MessageConsumer());
    }

    private Destination getSelfDestination() {
        return new Destination(Destination.Type.NODE, nodeConfiguration.getId());
    }

    public void start() {
        synchronized (threads) {
            if (!threads.isEmpty()) {
                throw new IllegalStateException("Already started");
            }
            if (acceptor != null) {
                threads.add(new Thread(acceptor));
            }
            if (replica != null) {
                threads.add(new Thread(replica));
            }
            if (leader != null) {
                threads.add(new Thread(leader));
            }
            threads.add(new ServerSocketListener(this));
            for (Thread thread : threads) {
                thread.start();
            }
        }
    }

    public void stop() {
        synchronized (threads) {
            for (Thread thread : threads) {
                thread.interrupt();
            }
            threads.clear();
        }
    }

    private class MessageConsumer implements Consumer<Message> {
        @Override
        public void consume(Message message) {
            Consumer<Message> consumer = consumers.get(message.getType());
            if (consumer == null) {
                log.error("No consumer registered for type {} (message: {})", message.getType(), message);
            } else {
                consumer.consume(message);
            }
        }
    }

}
