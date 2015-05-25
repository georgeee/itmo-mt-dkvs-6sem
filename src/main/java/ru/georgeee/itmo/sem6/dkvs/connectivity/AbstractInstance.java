package ru.georgeee.itmo.sem6.dkvs.connectivity;

import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.util.concurrent.*;

abstract class AbstractInstance implements Consumer<Message>, Runnable {
    private static final int QUEUE_CAPACITY = 1000;
    protected final BlockingQueue<Message> queue;
    private final ScheduledExecutorService repeatService;
    private final int repeatTimeout;
    final Node node;

    public AbstractInstance(Node node) {
        this.node = node;
        this.queue = createQueue();
        SystemConfiguration sysConfiguration = node.getConnectionManager().getSystemConfiguration();
        repeatService = Executors.newScheduledThreadPool(sysConfiguration.getInstanceRepeatPoolSize());
        repeatTimeout = sysConfiguration.getInstanceRepeatTimeout();
    }

    protected BlockingQueue<Message> createQueue() {
        return new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    }

    /**
     * Repeats action (after timeouts) until condition evaluates to true
     * @param predicate condition
     * @param runnable action
     */
    protected void executeRepeating(Predicate predicate, Runnable runnable){
        new RepeatingTask(predicate, runnable).run();
    }

    /**
     * Consumes object (immediately)
     *
     * @throws java.lang.IllegalStateException if queue is full
     */
    @Override
    public void consume(Message t) {
        this.queue.add(t);
    }

    protected abstract void consumeImpl(Message t);

    @Override
    public void run() {
        while (true) {
            try {
                Message t = queue.take();
                consumeImpl(t);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private class RepeatingTask implements Runnable{
        private final Predicate predicate;
        private final Runnable runnable;

        private RepeatingTask(Predicate predicate, Runnable runnable) {
            this.predicate = predicate;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            if(!predicate.evaluate()){
                runnable.run();
                repeatService.schedule(this, repeatTimeout, TimeUnit.MILLISECONDS);
            }
        }
    }

    interface Predicate {
        boolean evaluate();
    }
}
