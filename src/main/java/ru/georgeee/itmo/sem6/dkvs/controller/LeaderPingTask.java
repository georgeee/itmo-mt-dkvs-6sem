package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;

class LeaderPingTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LeaderPingTask.class);
    private final static String PREFIX = LeaderPingTask.class.getName() + "_";
    private final Runnable gainControlTask;
    private final Leader leader;
    private final AbstractController controller;
    private final int limit;

    //It's unlikely that this variable will be updated from more than one thread at once
    //So it's safe to make it volatile, not atomic
    private volatile int counter;
    private volatile int pingId;

    LeaderPingTask(Runnable gainControlTask, Leader leader, AbstractController controller) {
        this.gainControlTask = gainControlTask;
        this.leader = leader;
        this.controller = controller;
        this.limit = controller.getSystemConfiguration().getLeaderPingLimit();
    }

    @Override
    public void run() {
        try {
            final Destination mainLeader = leader.mainLeader;
            if (mainLeader != null) {
                ++counter;
                if (counter > limit) {
                    for (int i = pingId - limit - 1; i <= pingId; ++i) {
                        controller.removePingToken(mainLeader, getToken(i));
                    }
                    leader.mainLeader = null;
                    leader.addForExecution(gainControlTask);
                } else {
                    final int newPingId = ++pingId;
                    controller.removePingToken(mainLeader, getToken(newPingId - limit - 1));
                    controller.ping(mainLeader, getToken(newPingId), new Runnable() {
                        @Override
                        public void run() {
                            counter = 0;
                            //Removing old ping tokens
                            controller.removePingToken(mainLeader, getToken(newPingId - limit - 1));
                        }
                    });
                }
            } else {
                counter = 0;
            }
        }catch (Exception e){
            log.error("Leader ping task execution failed with exception", e);
        }
    }

    private String getToken(int i) {
        return PREFIX + i;
    }
}
