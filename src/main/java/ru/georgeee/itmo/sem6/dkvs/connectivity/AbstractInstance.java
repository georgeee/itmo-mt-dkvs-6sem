package ru.georgeee.itmo.sem6.dkvs.connectivity;

import ru.georgeee.itmo.sem6.dkvs.Consumer;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

abstract class AbstractInstance implements Consumer<Message>, Runnable {
    private static final int QUEUE_CAPACITY = 1000;
    protected final BlockingQueue<Message> queue;
    protected final Node node;

    public AbstractInstance(Node node) {
        this.node = node;
        this.queue = createQueue();
    }

    protected BlockingQueue<Message> createQueue() {
        return new ArrayBlockingQueue<Message>(QUEUE_CAPACITY);
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
}
