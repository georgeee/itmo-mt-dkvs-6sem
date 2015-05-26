package ru.georgeee.itmo.sem6.dkvs.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketListener implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(ServerSocketListener.class);

    private final ServerController server;

    ServerSocketListener(ServerController server) {
        this.server = server;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(server.getNodeConfiguration().getPort());
        } catch (IOException e) {
            log.error("Error opening socket on port " + server.getNodeConfiguration().getPort(), e);
            return;
        }
        while (true) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            Socket connectionSocket = null;
            try {
                connectionSocket = serverSocket.accept();
            } catch (IOException e) {
                log.error("Error while listening port " + server.getNodeConfiguration().getPort(), e);
            }
            if (connectionSocket != null) {
                final Socket finalConnectionSocket = connectionSocket;
                server.getConnectionManager().getReceiveListenersService().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ConnectionHandler.handleConnection(server.getConnectionManager(), finalConnectionSocket).run();
                        } catch (IOException e) {
                            try {
                                finalConnectionSocket.close();
                            } catch (IOException e1) {
                                log.error("Error while closing connection", e);
                            }
                            log.error("Error initializing connection", e);
                        }
                    }
                });
            }
        }
    }
}


