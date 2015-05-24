package ru.georgeee.itmo.sem6.dkvs.connectivity;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.Message;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ConnectionManager  {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    private final ExecutorService sendExecutor;
    @Getter
    private final SystemConfiguration systemConfiguration;
    @Getter
    private final Destination selfDestination;
    @Getter
    private final ConcurrentMap<Destination, ConnectionHandler> connections;
    @Getter
    private final Consumer<Message> consumer;

    public ConnectionManager(SystemConfiguration systemConfiguration, Destination selfDestination, Consumer<Message> consumer) {
        this.systemConfiguration = systemConfiguration;
        this.selfDestination = selfDestination;
        this.consumer = consumer;
        this.sendExecutor = Executors.newCachedThreadPool();
        this.connections = new ConcurrentHashMap<>();
    }

    public void send(final Destination destination, final Message message) {
        sendExecutor.submit(new SendTask(destination, message));
    }

    private final class SendTask implements Runnable {
        private final Destination destination;
        private final Message message;

        private SendTask(Destination destination, Message message) {
            this.destination = destination;
            this.message = message;
        }

        @Override
        public void run() {
            ConnectionHandler connectionHandler = connections.get(destination);
            if (connectionHandler == null) {
                try {
                    connectionHandler = ConnectionHandler.acquireConnection(ConnectionManager.this, destination);
                } catch (IOException | IllegalArgumentException e) {
                    log.error("Error to acquire a connection", e);
                }
                if (connectionHandler == null) {
                    log.error("Error to connect to node {}", destination);
                    log.error("Can't send a message (can't acquire a connection to {}): {}", destination, message);
                } else {
                    connectionHandler.send(message);
                }
            } else {
                connectionHandler.send(message);
            }
        }
    }


}
