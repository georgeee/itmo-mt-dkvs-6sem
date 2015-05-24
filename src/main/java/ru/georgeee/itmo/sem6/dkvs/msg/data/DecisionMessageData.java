package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Command;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;

import java.util.ArrayList;
import java.util.List;

public class DecisionMessageData extends AbstractMessageData {
    @Getter
    private final int slotId;
    @Getter
    private final Command command;

    public DecisionMessageData(int slotId, Command command) {
        this.slotId = slotId;
        this.command = command;
    }

    public DecisionMessageData(Message parent) throws MessageParsingException {
        try {
            this.slotId = Integer.parseInt(parent.getArgs()[0]);
            this.command = Command.parseFromArgs(parent.getArgs(), 1);
        } catch (NumberFormatException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.DECISION;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, slotId);
        appendToArgs(args, command);
        return args;
    }
}
