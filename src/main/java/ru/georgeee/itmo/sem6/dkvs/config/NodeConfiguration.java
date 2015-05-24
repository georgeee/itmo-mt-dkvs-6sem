package ru.georgeee.itmo.sem6.dkvs.config;

import lombok.Getter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

public class NodeConfiguration {
    @Getter
    private final String id;
    @Getter
    private final String host;
    @Getter
    private final int port;
    @Getter
    private final Set<Role> roles;

    public NodeConfiguration(String id, String host, int port, Set<Role> roles) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.roles = roles;
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(getHost());
    }
    public InetSocketAddress getInetSocketAddress() throws UnknownHostException {
        return new InetSocketAddress(getInetAddress(), getPort());
    }
}
