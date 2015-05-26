package ru.georgeee.itmo.sem6.dkvs.cli;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConvertible;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;

public class PingCommand implements ArgsConvertible{
    @Getter
    @ArgsField
    private final Destination destination;

    @Getter @ArgsField
    private final String token;

    public PingCommand(Destination destination, String token) {
        this.destination = destination;
        this.token = token;
    }
}
