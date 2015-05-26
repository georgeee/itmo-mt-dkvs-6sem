package ru.georgeee.itmo.sem6.dkvs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketListener implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(ServerSocketListener.class);

    private final ServerController controller;

    ServerSocketListener(ServerController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(controller.getNodeConfiguration().getPort());
        } catch (IOException e) {
            log.error("Error opening socket on port " + controller.getNodeConfiguration().getPort(), e);
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
                log.error("Error while listening port " + controller.getNodeConfiguration().getPort(), e);
            }
            if (connectionSocket != null) {
                log.debug("Received socket connection");
                final Socket finalConnectionSocket = connectionSocket;
                controller.getConnectionManager().getReceiveListenersService().submit(new Runnable() {
                    @Override
                    public void run() {
                        log.debug("Handling socket connection");
                        try {
                            ConnectionHandler.handleConnection(controller.getConnectionManager(), finalConnectionSocket).run();
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


