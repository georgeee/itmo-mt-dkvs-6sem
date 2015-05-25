package ru.georgeee.itmo.sem6.dkvs;

import ru.georgeee.itmo.sem6.dkvs.msg.Op;
import ru.georgeee.itmo.sem6.dkvs.msg.OpResult;

public interface StateHolder {
    OpResult applyOp(Op op);
}
