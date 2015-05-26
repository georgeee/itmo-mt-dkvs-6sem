package ru.georgeee.itmo.sem6.dkvs;

import ru.georgeee.itmo.sem6.dkvs.msg.OpResult;

public interface OpResultHandler {
    void run(int commandId, OpResult opResult);
}
