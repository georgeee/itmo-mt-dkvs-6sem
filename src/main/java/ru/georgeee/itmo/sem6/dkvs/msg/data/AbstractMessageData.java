package ru.georgeee.itmo.sem6.dkvs.msg.data;

import org.apache.commons.lang3.tuple.Pair;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsAppendable;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;
import ru.georgeee.itmo.sem6.dkvs.msg.PValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    protected Set<PValue> parsePValuesFromArgs(String[] args, int i) throws MessageParsingException {
        Set<PValue> pValues = new HashSet<>();
        while (i < args.length) {
            Pair<PValue, Integer> pValuePair = PValue.parseFromArgs(args, i);
            pValues.add(pValuePair.getLeft());
            i = pValuePair.getRight();
        }
        return pValues;
    }

}
