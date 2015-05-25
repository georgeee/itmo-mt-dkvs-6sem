package ru.georgeee.itmo.sem6.dkvs.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.util.Queue;
import java.util.concurrent.*;

abstract class AbstractInstance implements Consumer<Message>, Runnable {
    private static final Logger log = LoggerFactory.getLogger(AbstractInstance.class);
    private static final int QUEUE_CAPACITY = 1000;
    protected final BlockingQueue<Message> messageQueue;
    protected final Queue<RepeatingTask> repeatQueue;
    private final ScheduledExecutorService repeatService;
    private final int repeatTimeout;
    final Node node;

    public AbstractInstance(Node node) {
        SystemConfiguration sysConfiguration = node.getConnectionManager().getSystemConfiguration();
        this.node = node;
        this.messageQueue = createBlockingQueue();
        this.repeatQueue = new ConcurrentLinkedQueue<>();
        this.repeatService = Executors.newScheduledThreadPool(sysConfiguration.getInstanceRepeatPoolSize());
        this.repeatTimeout = sysConfiguration.getInstanceRepeatTimeout();
    }

    protected <T> BlockingQueue<T> createBlockingQueue() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    /**
     * Repeats action (after timeouts) until condition evaluates to true
     * Action is being executed in the same thread, by which messages are consumed
     * (so that we maintain single-thread access to instance structures)
     *
     * @param predicate condition
     * @param runnable  action
     */
    protected void executeRepeating(Predicate predicate, Runnable runnable, String commentary) {
        new RepeatingTask(predicate, runnable, commentary).run();
    }

    /**
     * Consumes object (immediately)
     *
     * @throws java.lang.IllegalStateException if messageQueue is full
     */
    @Override
    public void consume(Message t) {
        this.messageQueue.add(t);
    }

    protected abstract void consumeImpl(Message t);

    protected void sendToNode(Message message, String nodeId) {
        node.getConnectionManager().send(message, new Destination(Destination.Type.NODE, nodeId));
    }
    protected void send(Message message, Destination destination) {
        node.getConnectionManager().send(message, destination);
    }

    protected String getSelfId() {
        return node.getNodeConfiguration().getId();
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            processRepeats();
            try {
                try {
                    Message t = messageQueue.take();
                    consumeImpl(t);
                } catch (RuntimeException e) {
                    log.error("Error occurred consuming message by " + this.getClass(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processRepeats() {
        RepeatingTask task;
        while ((task = repeatQueue.poll()) != null) {
            task.doRepeat();
        }
    }

    private class RepeatingTask implements Runnable {
        private final Predicate predicate;
        private final Runnable runnable;
        private final String commentary;

        private RepeatingTask(Predicate predicate, Runnable runnable, String commentary) {
            this.predicate = predicate;
            this.runnable = runnable;
            this.commentary = commentary;
        }

        @Override
        public void run() {
            repeatQueue.add(this);
        }

        public void doRepeat() {
            if (!predicate.evaluate()) {
                log.info("Predicate evaluated to false, repeating: {}", commentary);
                runnable.run();
                repeatService.schedule(this, repeatTimeout, TimeUnit.MILLISECONDS);
            }
        }
    }

    protected static interface Predicate {
        boolean evaluate();
    }
}
