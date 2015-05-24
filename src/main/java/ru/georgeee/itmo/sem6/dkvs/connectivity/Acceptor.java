package ru.georgeee.itmo.sem6.dkvs.connectivity;


import ru.georgeee.itmo.sem6.dkvs.msg.Message;

class Acceptor extends AbstractInstance {

    public Acceptor(Node node) {
        super(node);
    }

    @Override
    protected void consumeImpl(Message message) {

    }
}
