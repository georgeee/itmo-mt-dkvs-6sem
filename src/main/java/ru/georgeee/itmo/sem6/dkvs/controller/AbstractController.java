package ru.georgeee.itmo.sem6.dkvs.controller;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.Utils;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.data.PingMessageData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ru.georgeee.itmo.sem6.dkvs.msg.Message.Type.PING;
import static ru.georgeee.itmo.sem6.dkvs.msg.Message.Type.PONG;

abstract class AbstractController {
    private static final Logger log = LoggerFactory.getLogger(AbstractController.class);
    protected final ConcurrentMap<Message.Type, Consumer<Message>> consumers;
    @Getter(AccessLevel.PACKAGE)
    private final ConnectionManager connectionManager;
    private final List<Thread> threads;
    private final PingPong pingPong;
    private final ConcurrentMap<Pair<Destination, String>, Runnable> onPongMap;

    public AbstractController(SystemConfiguration systemConfiguration) {
        this.threads = new ArrayList<>();
        this.connectionManager = new ConnectionManager(systemConfiguration, getSelfDestination(), new MessageConsumer());
        this.consumers = new ConcurrentHashMap<>();
        this.onPongMap = new ConcurrentHashMap<>();
        pingPong = new PingPong(this);
        Utils.putBatch(consumers, pingPong, PING, PONG);
    }

    protected abstract Destination.Type getDestinationType();

    protected abstract String getId();


    public void start() {
        synchronized (threads) {
            if (!threads.isEmpty()) {
                throw new IllegalStateException("Already started");
            }
            createThreads(threads);
            for (Thread thread : threads) {
                thread.start();
            }
        }
    }

    protected Destination getSelfDestination() {
        return new Destination(getDestinationType(), getId());
    }


    /**
     * Is being executed in lock
     *
     * @param threads thread list
     */
    protected abstract void createThreads(List<Thread> threads);

    public void stop() {
        synchronized (threads) {
            for (Thread thread : threads) {
                thread.interrupt();
            }
            threads.clear();
        }
    }

    protected void sendPing(Destination destination, String token) {
        Message message = new PingMessageData(getSelfDestination(), token).createMessage();
        connectionManager.send(message, destination);
    }

    public void ping(Destination destination, String token, Runnable onPong) {
        if (Utils.containsWhitespace(token)) {
            throw new IllegalArgumentException("Token shouldn't contain whitespace");
        }
        onPongMap.put(new ImmutablePair<>(destination, token), onPong);
        sendPing(destination, token);
    }

    protected void processPong(Destination sender, String token) {
        Runnable onPong = onPongMap.remove(new ImmutablePair<>(sender, token));
        if (onPong != null) {
            onPong.run();
        }
    }


    private class MessageConsumer implements Consumer<Message> {
        @Override
        public void consume(Message message) {
            log.info("Received message: {}", message);
            Consumer<Message> consumer = consumers.get(message.getType());
            if (consumer != null) {
                consumer.consume(message);
            } else {
                log.error("No consumer registered for type {} (message: {})", message.getType(), message);
            }
        }
    }


}
