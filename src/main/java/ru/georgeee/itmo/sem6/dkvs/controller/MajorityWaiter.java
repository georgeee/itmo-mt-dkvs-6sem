package ru.georgeee.itmo.sem6.dkvs.controller;

import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

abstract class MajorityWaiter {
    private final AbstractInstance instance;
    private final AbstractInstance.Predicate receivedEnoughPredicate;
    /**
     * List of acceptors, we wait for
     */
    private final Set<Destination> waitingSet;


    MajorityWaiter(Collection<Destination> originalSet, AbstractInstance instance) {
        this.instance = instance;
        this.waitingSet = new HashSet<>(originalSet);
        this.receivedEnoughPredicate = new CollectionSizePredicate(waitingSet, (waitingSet.size() + 1) / 2);
    }

    abstract Message getInitMessage();

    abstract String getName();

    public void init() {
        final Message message = getInitMessage();
        instance.executeRepeating(receivedEnoughPredicate, new Runnable() {
            @Override
            public void run() {
                for (Destination acceptor : waitingSet) {
                    instance.send(message, acceptor);
                }
            }
        }, getName() + " : waiting for responses from majority of recipients");
    }

    void stop() {
        waitingSet.clear();
    }

    boolean isReceivedEnough() {
        return receivedEnoughPredicate.evaluate();
    }

    void removeDestination(String nodeId) {
        removeDestination(new Destination(Destination.Type.NODE, nodeId));
    }

    void removeDestination(Destination destination) {
        waitingSet.remove(destination);
    }

    /**
     * Predicate: size of collection is less than limit
     */
    private static class CollectionSizePredicate implements AbstractInstance.Predicate {
        private final Collection collection;
        private final int limit;

        private CollectionSizePredicate(Collection collection, int limit) {
            this.collection = collection;
            this.limit = limit;
        }

        @Override
        public boolean evaluate() {
            return collection.size() < limit;
        }
    }
}
