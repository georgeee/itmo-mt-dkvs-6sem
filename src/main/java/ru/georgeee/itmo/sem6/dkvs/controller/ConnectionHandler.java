package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;
import ru.georgeee.itmo.sem6.dkvs.config.NodeConfiguration;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsConverter;
import ru.georgeee.itmo.sem6.dkvs.msg.Message;
import ru.georgeee.itmo.sem6.dkvs.msg.MessageParsingException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

class ConnectionHandler implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private static final int IDENTITY_STRING_PATTERN_CLIENT_GROUP = 0;
    private static final int IDENTITY_STRING_PATTERN_NODE_GROUP = 1;
    private static final int IDENTITY_STRING_PATTERN_VALUE_GROUP = 2;
    private final Socket socket;
    private final BufferedReader reader;
    private final Destination destination;
    private final BufferedWriter writer;
    private volatile boolean closed;
    private volatile ConnectionManager connectionManager;


    private ConnectionHandler(Socket socket, BufferedReader reader, BufferedWriter writer, Destination destination) throws IOException {
        log.debug("Established connection with {}", destination);
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.destination = destination;
    }

    private static BufferedWriter createWriter(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private static BufferedReader createReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    private static Destination readIdentity(BufferedReader reader) throws IOException {
        String identityString = reader.readLine();
        if (identityString != null) {
            try {
                return ArgsConverter.parse(Destination.class, identityString);
            } catch (RuntimeException e) {
                log.error("Wrong identity string: " + identityString);
                throw new IOException("Failed to retrieve identity string", e);
            }
        }
        throw new IOException("Failed to retrieve identity string");
    }

    public static ConnectionHandler handleConnection(ConnectionManager connectionManager, Socket socket) throws IOException {
        socket.setSoTimeout(connectionManager.getSystemConfiguration().getSocketTimeout());
        BufferedReader reader = createReader(socket);
        BufferedWriter writer = createWriter(socket);
        Destination destination = readIdentity(reader);
        ConnectionHandler connectionHandler = new ConnectionHandler(socket, reader, writer, destination);
        connectionHandler.register(connectionManager);
        return connectionHandler;
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
        Socket socket = new Socket();
        socket.connect(destConfiguration.getInetSocketAddress(), connectionManager.getSystemConfiguration().getSocketTimeout());
        socket.setSoTimeout(connectionManager.getSystemConfiguration().getSocketTimeout());
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
                log.warn("Error while closing connection", e2);
            }
            throw e;
        }
        ConnectionHandler connectionHandler = new ConnectionHandler(socket, reader, writer, destination);
        connectionHandler.register(connectionManager);
        return connectionHandler;
    }

    private static void writeIdentity(BufferedWriter writer, Destination selfDestination) throws IOException {
        writer.write(selfDestination.getType().name() + " " + selfDestination.getId());
        writer.newLine();
        writer.flush();
    }

    private void register(ConnectionManager connectionManager) throws SocketException {
        if (this.connectionManager != null) {
            throw new IllegalStateException("Already registered");
        }
        this.connectionManager = connectionManager;
        ConnectionHandler previous = connectionManager.getConnections().put(destination, this);
        if (previous != null) {
            previous.close();
        }
    }

    @Override
    public void run() {
        try {
            log.debug("Started listening for {}", destination);
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
            log.debug("Stopped listening for {}", destination);
        } catch (IOException e) {
            log.debug("I/O exception caught", e);
        }
        close();
    }

    public boolean trySend(final Message message) {
        if (!closed) {
            try {
                writer.write(message.toString());
                writer.newLine();
                writer.flush();
                return true;
            } catch (IOException e) {
                log.debug("Error sending message, " + message, e);
            }
        }
        return false;
    }

    private synchronized void close() {
        if (!closed) {
            try {
                closed = true;
                log.debug("Closing connection for {}", destination);
                reader.close();
                writer.close();
                socket.close();
            } catch (IOException e) {
                log.warn("Error while closing connection", e);
            }
            connectionManager.getConnections().remove(destination, this);
        }
    }
}
