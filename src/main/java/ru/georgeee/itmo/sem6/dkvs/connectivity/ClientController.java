package ru.georgeee.itmo.sem6.dkvs.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.Utils;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;

import java.util.List;
import java.util.UUID;

import static ru.georgeee.itmo.sem6.dkvs.msg.Message.Type.RESPONSE;

public class ClientController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);
    private final Client client;
    private final String id;

    public ClientController(SystemConfiguration systemConfiguration, String id) {
        super(systemConfiguration);
        this.id = UUID.randomUUID().toString();
        this.client = new Client(this);
        Utils.putBatch(consumers, client, RESPONSE);
    }

    @Override
    protected Destination.Type getDestinationType() {
        return Destination.Type.CLIENT;
    }

    @Override
    protected String getId() {
        return id;
    }

    @Override
    protected void createThreads(List<Thread> threads) {
        threads.add(new Thread(client));
    }

}
