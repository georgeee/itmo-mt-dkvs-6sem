package ru.georgeee.itmo.sem6.dkvs.controller;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.io.IOException;
import java.util.concurrent.*;

class ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    private final ScheduledExecutorService sendExecutor;
    @Getter
    private final ExecutorService receiveListenersService;
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
        this.sendExecutor = Executors.newScheduledThreadPool(systemConfiguration.getSenderPoolSize());
        this.connections = new ConcurrentHashMap<>();
        this.receiveListenersService = Executors.newCachedThreadPool();
    }

    public void send(Message message, Destination destination) {
        send(new SendTask(message, destination));
    }

    private void send(SendTask task) {
        int counter = task.counter;
        if (counter > systemConfiguration.getMessageRetry()) {
            log.warn("Retry count exceed for message to {} (message: {})", task.destination, task.message);
        } else {
            task.incCounter();
            if (counter == 0) {
                log.info("Sending message to {}: {}", task.destination, task.message);
                sendExecutor.submit(task);
            } else {
                log.info("Retrying sending message ({}) to {} (message: {})", counter, task.destination, task.message);
                sendExecutor.schedule(task, counter * systemConfiguration.getMessageRetryTimeout(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private final class SendTask implements Runnable {
        private final Message message;
        private final Destination destination;
        private int counter;

        private SendTask(Message message, Destination destination) {
            this.message = message;
            this.destination = destination;
        }


        public void incCounter() {
            ++counter;
        }

        @Override
        public void run() {
            ConnectionHandler connectionHandler = connections.get(destination);
            if (connectionHandler == null) {
                try {
                    connectionHandler = ConnectionHandler.acquireConnection(ConnectionManager.this, destination);
                } catch (IOException | IllegalArgumentException e) {
                    log.info("Error to acquire a connection", e);
                }
                if (connectionHandler != null) {
                    getReceiveListenersService().submit(connectionHandler);
                } else {
                    log.info("Error to connect to node {}", destination);
                    log.info("Can't send a message (can't acquire a connection to {}): {}", destination, message);
                }
            }
            if (connectionHandler != null) {
                log.debug("Trying to perform send to {} (message: {})", destination, message);
                boolean isSent = connectionHandler.trySend(message);
                if (isSent) {
                    log.debug("Successfully performed send to {} (message: {})", destination, message);
                } else {
                    log.debug("Failed to perform send to {} (message: {}), putting for retry", destination, message);
                    send(this);
                }
            }
        }
    }


}
