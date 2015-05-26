package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.util.Queue;
import java.util.concurrent.*;

abstract class AbstractInstance implements Consumer<Message>, Runnable {
    protected static final Predicate TRUE_PREDICATE = new Predicate() {
        @Override
        public boolean evaluate() {
            return true;
        }
    };
    private static final Logger log = LoggerFactory.getLogger(AbstractInstance.class);
    private static final int QUEUE_CAPACITY = 1000;
    protected final BlockingQueue<Message> messageQueue;
    protected final Queue<RepeatingTask> repeatQueue;
    private final AbstractController controller;
    private final ScheduledExecutorService repeatService;
    private final int repeatTimeout;

    public AbstractInstance(AbstractController controller) {
        SystemConfiguration sysConfiguration = controller.getSystemConfiguration();
        this.controller = controller;
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
     * <p/>
     * Method is intended to be used only to tasks, which are critical for liveness.
     *
     * @param predicate condition
     * @param task      action
     */
    protected void executeRepeating(Predicate predicate, Runnable task, String commentary) {
        new RepeatingTask(predicate, task, commentary).run();
    }

    protected void addForExecution(Runnable task) {
        new RepeatingTask(TRUE_PREDICATE, task, "").run();
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
        controller.getConnectionManager().send(message, new Destination(Destination.Type.NODE, nodeId));
    }

    protected void send(Message message, Destination destination) {
        controller.getConnectionManager().send(message, destination);
    }

    protected String getSelfId() {
        return controller.getId();
    }

    public Destination getSelfDestination() {
        return controller.getSelfDestination();
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
                Message message = null;
                try {
                    message = messageQueue.take();
                    consumeImpl(message);
                } catch (RuntimeException e) {
                    log.error("Error occurred consuming message " + message + " by " + this.getClass(), e);
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
            boolean needRepeat = task.doRepeat();
            if (needRepeat) {
                repeatService.schedule(task, repeatTimeout, TimeUnit.MILLISECONDS);
            }
        }
    }

    protected static interface Predicate {
        boolean evaluate();
    }

    private final class RepeatingTask implements Runnable {
        private final Predicate predicate;
        private final Runnable runnable;
        private final String commentary;
        private volatile boolean firstRun = true;

        private RepeatingTask(Predicate predicate, Runnable runnable, String commentary) {
            this.predicate = predicate;
            this.runnable = runnable;
            this.commentary = commentary;
        }

        @Override
        public void run() {
            repeatQueue.add(this);
        }

        public boolean doRepeat() {
            if (firstRun) {
                firstRun = false;
                log.info("Executing for the first time task; {}", commentary);
                runnable.run();
                return false;
            }
            if (!predicate.evaluate()) {
                log.info("Predicate evaluated to false, repeating: {}", commentary);
                runnable.run();
                return true;
            }
            return false;
        }
    }
}
