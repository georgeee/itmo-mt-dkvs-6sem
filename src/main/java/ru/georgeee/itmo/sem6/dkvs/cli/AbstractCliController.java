package ru.georgeee.itmo.sem6.dkvs.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.Utils;
import ru.georgeee.itmo.sem6.dkvs.controller.Controller;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConverter;

import java.io.*;
import java.util.Arrays;

public abstract class AbstractCliController {
    private static final Logger log = LoggerFactory.getLogger(AbstractCliController.class);
    private final Controller controller;
    private final BufferedReader in;
    public final PrintStream out;

    public AbstractCliController(Controller controller) {
        this(controller, createBufferedReader(System.in), System.out);
    }

    private static BufferedReader createBufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }

    public AbstractCliController(Controller controller, BufferedReader in, PrintStream out) {
        this.controller = controller;
        this.in = in;
        this.out = out;
    }

    public void listen() throws IOException {
        String line;
        while((line = in.readLine()) != null){
            if(!line.trim().isEmpty()) {
                String[] args = Utils.splitToArgs(line);
                processInput(args);
            }
        }
    }

    protected void processInput(String[] args) {
        try {
            switch (args[0].toLowerCase()) {
                case "ping":
                    processPing(args);
                    break;
                case "start":
                    controller.start();
                    break;
                case "stop":
                    controller.stop();
                    break;
                default:
                    processInputCustom(args);
            }
        } catch (Exception e) {
            log.error("Error processing input: " + Arrays.toString(args), e);
        }
    }

    protected abstract void processInputCustom(String[] args);

    protected void processPing(String[] args) {
        PingCommand pingCommand = ArgsConverter.parse(PingCommand.class, args, 1);
        final Destination destination = pingCommand.getDestination();
        final String token = pingCommand.getToken();
        controller.ping(destination, token, new Runnable() {
            @Override
            public void run() {
                out.format("PONG from %s (token %s)%n", destination, token);
            }
        });
    }
}
