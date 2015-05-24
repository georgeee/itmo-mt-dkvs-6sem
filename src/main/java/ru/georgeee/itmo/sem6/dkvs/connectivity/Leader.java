package ru.georgeee.itmo.sem6.dkvs.connectivity;

import ru.georgeee.itmo.sem6.dkvs.msg.Message;

class Leader extends AbstractInstance {

    public Leader(Node node) {
        super(node);
    }

    @Override
    protected void consumeImpl(Message message) {

    }
}
