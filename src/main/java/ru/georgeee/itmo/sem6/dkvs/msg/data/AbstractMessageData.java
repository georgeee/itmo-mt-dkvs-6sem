package ru.georgeee.itmo.sem6.dkvs.msg.data;

import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConverter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConvertible;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

abstract class AbstractMessageData implements ArgsConvertible{

    public Message createMessage() {
        return new Message(getType(), getArgs());
    }

    protected abstract Message.Type getType();

    protected String[] getArgs(){
        return ArgsConverter.getArgs(this);
    }

}
