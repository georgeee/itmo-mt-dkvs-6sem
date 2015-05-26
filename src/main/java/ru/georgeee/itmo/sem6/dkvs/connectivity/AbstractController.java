package ru.georgeee.itmo.sem6.dkvs.connectivity;

import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.Utils;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static ru.georgeee.itmo.sem6.dkvs.msg.Message.Type.*;
abstract class AbstractController {
    private static final Logger log = LoggerFactory.getLogger(AbstractController.class);
    protected final Map<Message.Type, Consumer<Message>> consumers;
    @Getter(AccessLevel.PACKAGE)
    private final ConnectionManager connectionManager;
    private final List<Thread> threads;
    private final PingPong pingPong;

    public AbstractController(SystemConfiguration systemConfiguration) {
        this.threads = new ArrayList<>();
        this.connectionManager = new ConnectionManager(systemConfiguration, getSelfDestination(), new MessageConsumer());
        this.consumers = new EnumMap<>(Message.Type.class);
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


    protected abstract void createThreads(List<Thread> threads);

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
            if (consumer != null) {
                consumer.consume(message);
            } else {
                log.error("No consumer registered for type {} (message: {})", message.getType(), message);
            }
        }
    }

}
