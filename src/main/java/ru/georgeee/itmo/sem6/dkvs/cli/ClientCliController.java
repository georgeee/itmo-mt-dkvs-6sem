package ru.georgeee.itmo.sem6.dkvs.cli;

import ru.georgeee.itmo.sem6.dkvs.OpResultHandler;
import ru.georgeee.itmo.sem6.dkvs.controller.ClientController;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConverter;
import ru.georgeee.itmo.sem6.dkvs.msg.Op;
import ru.georgeee.itmo.sem6.dkvs.msg.OpResult;

public class ClientCliController extends AbstractCliController {
    private final ClientController controller;

    public ClientCliController(ClientController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    protected void processInputCustom(String[] args) {
        Op op = ArgsConverter.parse(Op.class, args);
        int commandId = controller.request(op, new OpResultHandler() {
            @Override
            public void run(int commandId, OpResult opResult) {
                out.format("Response for #%d: %s%n", commandId, opResult);
            }
        });
        out.format("Request received id: #%d%n", commandId);
    }
}
