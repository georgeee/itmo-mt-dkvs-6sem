package ru.georgeee.itmo.sem6.dkvs;

import org.apache.commons.configuration.ConfigurationException;
import ru.georgeee.itmo.sem6.dkvs.config.NodeConfiguration;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.connectivity.ServerController;

import java.io.File;

public class Main {
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

    private void launchClientInteractive() {

    }

    private void launchNode(String id) {
        NodeConfiguration nodeConfiguration = configuration.getNodes().get(id);
        if (nodeConfiguration == null) {
            throw new IllegalArgumentException("Unknown node " + id);
        }
        ServerController server = new ServerController(configuration, nodeConfiguration);
        server.start();
    }
}
