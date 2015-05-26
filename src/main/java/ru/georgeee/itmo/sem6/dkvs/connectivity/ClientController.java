package ru.georgeee.itmo.sem6.dkvs.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.OpResultHandler;
import ru.georgeee.itmo.sem6.dkvs.Utils;
import ru.georgeee.itmo.sem6.dkvs.config.SystemConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.Op;
import ru.georgeee.itmo.sem6.dkvs.msg.OpResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.georgeee.itmo.sem6.dkvs.msg.Message.Type.RESPONSE;

public class ClientController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ClientController.class);
    private final Client client;
    private final String id;
    private final AtomicInteger commandIdCounter;
    private final ConcurrentMap<Integer, OpResultHandler> opResultHandlers;

    public ClientController(SystemConfiguration systemConfiguration, String id) {
        super(systemConfiguration);
        this.commandIdCounter = new AtomicInteger();
        this.id = UUID.randomUUID().toString();
        this.client = new Client(this);
        Utils.putBatch(consumers, client, RESPONSE);
        opResultHandlers = new ConcurrentHashMap<>();
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

    public int request(Op op, OpResultHandler resultHandler) {
        int commandId = commandIdCounter.incrementAndGet();
        opResultHandlers.put(commandId, resultHandler);
        client.submitRequest(commandId, op);
        return commandId;
    }

    void processResponse(int commandId, OpResult opResult) {
        OpResultHandler resultHandler = opResultHandlers.remove(commandId);
        if (resultHandler != null) {
            resultHandler.run(commandId, opResult);
        }
    }


}
