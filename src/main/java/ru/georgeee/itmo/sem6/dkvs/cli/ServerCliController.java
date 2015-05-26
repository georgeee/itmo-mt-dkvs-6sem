package ru.georgeee.itmo.sem6.dkvs.cli;

import ru.georgeee.itmo.sem6.dkvs.controller.Controller;

import java.io.BufferedReader;
import java.io.PrintStream;

public class ServerCliController extends AbstractCliController {
    public ServerCliController(Controller controller) {
        super(controller);
    }

    public ServerCliController(Controller controller, BufferedReader in, PrintStream out) {
        super(controller, in, out);
    }

    @Override
    protected void processInputCustom(String[] args) {

    }
}
