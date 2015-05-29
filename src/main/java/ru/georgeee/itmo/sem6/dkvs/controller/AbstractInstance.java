package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.utils.Either;

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
    protected final BlockingQueue<Either<Message, RepeatingTask>> queue;
    private final AbstractController controller;
    private final ScheduledExecutorService repeatService;
    private final int repeatTimeout;

    public AbstractInstance(AbstractController controller) {
        SystemConfiguration sysConfiguration = controller.getSystemConfiguration();
        this.controller = controller;
        this.queue = createBlockingQueue();
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
    protected void executeRepeating(Predicate predicate, Runnable task, String commentary, boolean firstRun) {
        RepeatingTask repeatingTask = new DefaultRepeatingTask(predicate, task, commentary, firstRun);
        if (firstRun) {
            repeatingTask.run();
        } else {
            setForRepeat(repeatingTask);
        }
    }

    private void setForRepeat(RepeatingTask task) {
        repeatService.schedule(task, repeatTimeout, TimeUnit.MILLISECONDS);
    }

    protected void executeRepeating(Predicate predicate, Runnable task, String commentary) {
        executeRepeating(predicate, task, commentary, true);
    }

    protected void addForExecution(Runnable task) {
        new OneTimeRepeatingTask(task).run();
    }

    /**
     * Consumes object (immediately)
     *
     * @throws java.lang.IllegalStateException if queue is full
     */
    @Override
    public void consume(Message t) {
        this.queue.add(new Either.Left<Message, RepeatingTask>(t));
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
            try {
                Either<Message, RepeatingTask> either = null;
                try {
                    either = queue.take();
                    if (either instanceof Either.Left) {
                        consumeImpl(either.getLeft());
                    } else {
                        processRepeat(either.getRight());
                    }
                } catch (RuntimeException e) {
                    log.error("Error occurred consuming " + either + " by " + this.getClass(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processRepeat(RepeatingTask task) {
        boolean needRepeat = task.doRepeat();
        if (needRepeat) {
            setForRepeat(task);
        }
    }

    protected void addTimerTask(Runnable runnable, int delay) {
        repeatService.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MILLISECONDS);
    }

    protected static interface Predicate {
        boolean evaluate();
    }

    private class DefaultRepeatingTask extends RepeatingTask {
        private final Predicate predicate;
        private final Runnable runnable;
        private final String commentary;
        private volatile boolean firstRun;

        private DefaultRepeatingTask(Predicate predicate, Runnable runnable, String commentary, boolean firstRun) {
            this.predicate = predicate;
            this.runnable = runnable;
            this.commentary = commentary;
            this.firstRun = firstRun;
        }

        public boolean doRepeat() {
            if (firstRun) {
                firstRun = false;
                log.debug("Executing for the first time task, repeat commentary: {}", commentary);
                runnable.run();
                return true;
            }
            if (!predicate.evaluate()) {
                log.info("Predicate evaluated to false, repeating: {}", commentary);
                runnable.run();
                return true;
            }
            return false;
        }
    }

    private class OneTimeRepeatingTask extends RepeatingTask {
        private final Runnable runnable;

        private OneTimeRepeatingTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public boolean doRepeat() {
            runnable.run();
            return false;
        }
    }

    private abstract class RepeatingTask implements Runnable {

        @Override
        public void run() {
            queue.add(new Either.Right<Message, RepeatingTask>(this));
        }

        public abstract boolean doRepeat();
    }
}
