package ru.georgeee.itmo.sem6.dkvs.connectivity;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.DestinatedMessage;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ConnectionManager {
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

    public void send(final DestinatedMessage message) {
        message.incCounter();
        if (message.getCounter() > systemConfiguration.getMessageRetry()) {
            log.warn("Retry count exceed for message {}", message);
        } else {
            sendExecutor.submit(new SendTask(message));
        }
    }

    private final class SendTask implements Runnable {
        private final DestinatedMessage message;

        private SendTask(DestinatedMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            ConnectionHandler connectionHandler = connections.get(message.getDestination());
            if (connectionHandler == null) {
                try {
                    connectionHandler = ConnectionHandler.acquireConnection(ConnectionManager.this, message.getDestination());
                } catch (IOException | IllegalArgumentException e) {
                    log.error("Error to acquire a connection", e);
                }
                if (connectionHandler == null) {
                    log.error("Error to connect to node {}", message.getDestination());
                    log.error("Can't send a message (can't acquire a connection to {}): {}", message.getDestination(), message);
                } else {
                    connectionHandler.send(message);
                }
            } else {
                connectionHandler.send(message);
            }
        }
    }


}
