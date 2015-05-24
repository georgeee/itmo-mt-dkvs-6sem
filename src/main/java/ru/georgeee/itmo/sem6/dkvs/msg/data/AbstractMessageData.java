package ru.georgeee.itmo.sem6.dkvs.msg.data;

import ru.georgeee.itmo.sem6.dkvs.msg.ArgsAppendable;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

import java.util.List;

abstract class AbstractMessageData {

    public Message createMessage() {
        List<String> args = getArgs();
        return new Message(getType(), args.toArray(new String[args.size()]));
    }

    protected abstract Message.Type getType();

    protected abstract List<String> getArgs();

    protected void appendToArgs(List<String> args, String string) {
        args.add(string);
    }

    protected void appendToArgs(List<String> args, Number num) {
        args.add(num.toString());
    }

    protected void appendToArgs(List<String> args, ArgsAppendable appendable) {
        appendable.appendToArgs(args);
    }
}
