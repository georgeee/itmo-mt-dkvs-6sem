package ru.georgeee.itmo.sem6.dkvs;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.cli.AbstractCliController;
import ru.georgeee.itmo.sem6.dkvs.cli.ClientCliController;
import ru.georgeee.itmo.sem6.dkvs.cli.ServerCliController;
import ru.georgeee.itmo.sem6.dkvs.config.NodeConfiguration;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.controller.ClientController;
import ru.georgeee.itmo.sem6.dkvs.controller.ServerController;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_PROPERTIES_FILE = "dkvs.properties";
    private final SystemConfiguration configuration;

    public Main(SystemConfiguration configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args) throws ConfigurationException {
        String propertiesFile = DEFAULT_PROPERTIES_FILE;
        int i;
        argsLoop:
        for (i = 0; i < args.length; ++i) {
            String arg = args[i];
            switch (arg) {
                case "-c":
                    propertiesFile = args[++i];
                    break;
                default:
                    break argsLoop;
            }
        }
        Main instance = new Main(new SystemConfiguration(new File(propertiesFile)));
        String action = args[i++];
        switch (action) {
            case "node":
                instance.launchNode(args[i]);
                break;
            case "client":
                instance.launchClientInteractive();
                break;
        }
    }

    private void listen(AbstractCliController cliController) {
        while (true) {
            try {
                cliController.listen();
            } catch (IOException e) {
                log.error("Error occurred while listening: retrying", e);
            }
        }
    }

    private void launchClientInteractive() {
        ClientController controller = new ClientController(configuration);
        controller.init();
        controller.start();
        listen(new ClientCliController(controller));
    }

    private void launchNode(String id) {
        NodeConfiguration nodeConfiguration = configuration.getNodes().get(id);
        if (nodeConfiguration == null) {
            throw new IllegalArgumentException("Unknown node " + id);
        }
        ServerController controller = new ServerController(configuration, nodeConfiguration);
        controller.init();
        controller.start();
        listen(new ServerCliController(controller));
    }
}
