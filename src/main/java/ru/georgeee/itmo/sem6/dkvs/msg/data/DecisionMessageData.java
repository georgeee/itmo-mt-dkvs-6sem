package ru.georgeee.itmo.sem6.dkvs.msg.data;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.*;

public class DecisionMessageData extends AbstractMessageData {
    @Getter @ArgsField
    private final int slotId;
    @Getter @ArgsField
    private final Command command;

    @ArgsConstructor
    public DecisionMessageData(int slotId, Command command) {
        this.slotId = slotId;
        this.command = command;
    }

    @Override
    protected Message.Type getType() {
        return Message.Type.DECISION;
    }
}
