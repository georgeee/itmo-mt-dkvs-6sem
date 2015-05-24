package ru.georgeee.itmo.sem6.dkvs.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ServerSocketListener extends Thread {
    private final static Logger log = LoggerFactory.getLogger(ServerSocketListener.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Node parent;

    ServerSocketListener(Node parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(parent.getNodeConfiguration().getPort());
        } catch (IOException e) {
            log.error("Error opening socket on port " + parent.getNodeConfiguration().getPort(), e);
            return;
        }
        while (true) {
            Socket connectionSocket = null;
            try {
                connectionSocket = serverSocket.accept();
            } catch (IOException e) {
                log.error("Error while listening port " + parent.getNodeConfiguration().getPort(), e);
            }
            if (connectionSocket != null) {
                try {
                    executorService.submit(ConnectionHandler.handleConnection(parent.getConnectionManager(), connectionSocket));
                } catch (IOException e) {
                    try {
                        connectionSocket.close();
                    } catch (IOException e1) {
                        log.error("Error while closing connection", e);
                    }
                    log.error("Error initializing connection", e);
                }
            }
        }
    }
}


