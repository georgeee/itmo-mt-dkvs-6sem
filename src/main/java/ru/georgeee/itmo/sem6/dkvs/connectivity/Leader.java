package ru.georgeee.itmo.sem6.dkvs.connectivity;

import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;

import java.util.HashMap;
import java.util.Map;

class Leader extends AbstractInstance {
    private final Map<PValue, Commander> commanders;
    private final Map<Integer, Scout> scouts;

    public Leader(Node node) {
        super(node);
        commanders = new HashMap<>();
        scouts = new HashMap<>();
    }

    @Override
    protected void consumeImpl(Message message) {

    }

    private class Commander{

    }

    private class Scout {
    }
}
