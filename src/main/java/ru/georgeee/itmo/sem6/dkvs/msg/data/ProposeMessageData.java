package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConstructor;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;
import ru.georgeee.itmo.sem6.dkvs.msg.Command;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;

public class ProposeMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final int slotId;
    @Getter @ArgsField
    private final Command command;

    @ArgsConstructor
    public ProposeMessageData(int slotId, Command command) {
        this.slotId = slotId;
        this.command = command;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.PROPOSE;
    }

}
