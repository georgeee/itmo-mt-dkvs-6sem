package ru.georgeee.itmo.sem6.dkvs.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.NodeConfiguration;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.connectivity.msg.MessageParsingException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ConnectionHandler implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private static final Pattern IDENTITY_STRING_PATTERN = Pattern.compile("^\\s*(?:(client)|(node))\\s+(\\S+)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final int IDENTITY_STRING_PATTERN_CLIENT_GROUP = 0;
    private static final int IDENTITY_STRING_PATTERN_NODE_GROUP = 1;
    private static final int IDENTITY_STRING_PATTERN_VALUE_GROUP = 2;
    private final ConnectionManager connectionManager;
    private final ExecutorService sendExecutor;
    private final Socket socket;
    private final BufferedReader reader;
    private final Destination destination;
    private final BufferedWriter writer;
    private volatile boolean closed;


    private ConnectionHandler(ConnectionManager connectionManager, Socket socket, BufferedReader reader, BufferedWriter writer, Destination destination) throws IOException {
        this.connectionManager = connectionManager;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.destination = destination;
        this.sendExecutor = Executors.newSingleThreadExecutor();
        registerConnection();
    }

    private static BufferedWriter createWriter(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private static BufferedReader createReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    private void registerConnection() {
        ConnectionHandler previous = connectionManager.getConnections().put(destination, this);
        if (previous != null) {
            previous.close();
        }
    }

    private static Destination readIdentity(BufferedReader reader) throws IOException {
        String identityString = reader.readLine();
        if (identityString != null) {
            Matcher matcher = IDENTITY_STRING_PATTERN.matcher(identityString);
            if (matcher.find()) {
                String id = matcher.group(IDENTITY_STRING_PATTERN_VALUE_GROUP);
                if (matcher.group(IDENTITY_STRING_PATTERN_CLIENT_GROUP) != null) {
                    return new Destination(Destination.Type.CLIENT, id);
                } else if (matcher.group(IDENTITY_STRING_PATTERN_NODE_GROUP) != null) {
                    return new Destination(Destination.Type.NODE, id);
                }
            }
            log.error("Wrong identity string: {}", identityString);
        }
        throw new IOException("Failed to retrieve identity string");
    }

    public static ConnectionHandler handleConnection(ConnectionManager connectionManager, Socket socket) throws IOException {
        BufferedReader reader = createReader(socket);
        BufferedWriter writer = createWriter(socket);
        Destination destination = readIdentity(reader);
        return new ConnectionHandler(connectionManager, socket, reader, writer, destination);
    }

    @Override
    public void run() {
        try {
            while (!closed) {
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                    break;
                }
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                try {
                    Message message = Message.parseMessage(line);
                    connectionManager.getConsumer().consume(message);
                } catch (MessageParsingException e) {
                    log.error("Wrong message received: " + line, e);
                }
            }
        } catch (IOException e) {
            log.error("I/O exception caught", e);
        }
        close();
    }

    public void send(final Message message) {
        sendExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (closed) {
                    //resend
                    connectionManager.send(destination, message);
                } else {
                    try {
                        writer.write(message.toString());
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        log.error("Error sending message, " + message, e);
                        connectionManager.send(destination, message);
                    }
                }
            }
        });
    }

    private synchronized void close() {
        if (!closed) {
            try {
                closed = true;
                log.info("Closing connection");
                reader.close();
                writer.close();
                socket.close();
            } catch (IOException e) {
                log.error("Error while closing connection", e);
            }
            connectionManager.getConnections().remove(destination, this);
        }
    }

    /**
     * @throws java.lang.IllegalArgumentException if destination isn't node or id is unknown by system configuration
     */
    public static ConnectionHandler acquireConnection(ConnectionManager connectionManager, Destination destination) throws IOException {
        if (destination.getType() != Destination.Type.NODE) {
            throw new IllegalArgumentException("Can't acquire connection to client");
        }
        NodeConfiguration destConfiguration = connectionManager.getSystemConfiguration().getNodes().get(destination.getId());
        if (destConfiguration == null) {
            throw new IllegalArgumentException("Unknown destination node: " + destination.getId());
        }
        Socket socket = new Socket(destConfiguration.getAddress(), destConfiguration.getPort());
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = createReader(socket);
            writer = createWriter(socket);
            writeIdentity(writer, connectionManager.getSelfDestination());
        } catch (IOException | RuntimeException e) {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                socket.close();
            } catch (IOException e2) {
                log.error("Error while closing connection", e2);
            }
            throw e;
        }
        return new ConnectionHandler(connectionManager, socket, reader, writer, destination);
    }

    private static void writeIdentity(BufferedWriter writer, Destination selfDestination) throws IOException {
        writer.write(selfDestination.getType().name() + " " + selfDestination.getId());
        writer.newLine();
        writer.flush();
    }
}
