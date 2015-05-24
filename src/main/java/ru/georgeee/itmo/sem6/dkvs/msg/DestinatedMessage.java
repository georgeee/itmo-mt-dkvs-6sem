package ru.georgeee.itmo.sem6.dkvs.msg;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.Destination;

public class DestinatedMessage {
    @Getter
    private final Destination destination;
    @Getter
    private final Message message;

    @Getter
    private int counter;

    public DestinatedMessage(Destination destination, Message message) {
        this.destination = destination;
        this.message = message;
    }

    public void incCounter() {
        ++counter;
    }
}
