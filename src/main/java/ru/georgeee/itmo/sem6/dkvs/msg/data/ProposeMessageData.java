package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.Command;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;

import java.util.ArrayList;
import java.util.List;

public class ProposeMessageData extends AbstractMessageData {
    @Getter
    private final int slotId;
    @Getter
    private final Command command;

    public ProposeMessageData(int slotId, Command command) {
        this.slotId = slotId;
        this.command = command;
    }

    public ProposeMessageData(Message parent) throws MessageParsingException {
        try {
            this.slotId = Integer.parseInt(parent.getArgs()[0]);
            this.command = Command.parseFromArgs(parent.getArgs(), 1).getLeft();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new MessageParsingException(parent.getArgs());
        }
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.PROPOSE;
    }

    @Override
    protected List<String> getArgs() {
        List<String> args = new ArrayList<>();
        appendToArgs(args, slotId);
        appendToArgs(args, command);
        return args;
    }

}
