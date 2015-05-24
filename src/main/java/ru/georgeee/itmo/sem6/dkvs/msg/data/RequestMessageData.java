package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Command;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;

import java.util.ArrayList;
import java.util.List;

public class RequestMessageData extends AbstractMessageData {
    @Getter
    private final Command command;

    public RequestMessageData(Command command) {
        this.command = command;
    }

    public RequestMessageData(Message parent) throws MessageParsingException {
        this.command = Command.parseFromArgs(parent.getArgs(), 0);
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.REQUEST;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, command);
        return args;
    }
}
