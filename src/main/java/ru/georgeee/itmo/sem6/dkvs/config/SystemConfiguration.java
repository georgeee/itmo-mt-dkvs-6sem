package ru.georgeee.itmo.sem6.dkvs.config;

import lombok.Getter;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem6.dkvs.Destination;

import java.io.File;
import java.util.*;

public class SystemConfiguration {
    private final static Logger log = LoggerFactory.getLogger(SystemConfiguration.class);
    private static final String NODE_PREFIX = "node.";
    private static final String ROLES_PREFIX = "roles.";
    private static final String SOCKET_TIMEOUT_KEY = "socket.timeout";
    private static final String MESSAGE_RETRY_KEY = "message.retry";
    private static final String MESSAGE_RETRY_TIMEOUT_KEY = "message.retry.timeout";
    private static final String PAXOS_SLOT_WIDOW_KEY = "paxos.slot.window";
    private static final String SENDER_POOL_SIZE_KEY = "sender.poolSize";
    private static final String INSTANCE_REPEAT_POOL_SIZE_KEY = "instance.repeat.poolSize";
    private static final String INSTANCE_REPEAT_TIMEOUT_KEY = "instance.repeat.timeout";

    @Getter
    private final Map<String, NodeConfiguration> nodes;
    @Getter
    private final int socketTimeout;
    @Getter
    private final int messageRetry;
    @Getter
    private final int messageRetryTimeout;
    @Getter
    private final int paxosSlotWindow;
    @Getter
    private final int senderPoolSize;
    @Getter
    private final int instanceRepeatTimeout;
    @Getter
    private final int instanceRepeatPoolSize;

    public SystemConfiguration(File propertiesFile) throws ConfigurationException {
        this(new PropertiesConfiguration(propertiesFile));
    }

    public SystemConfiguration(Configuration configuration) {
        @SuppressWarnings("unchecked")
        Iterator<String> keyIterator = configuration.getKeys("node.");
        nodes = new HashMap<>();
        while (keyIterator.hasNext()) {
            String id = keyIterator.next().substring(NODE_PREFIX.length());
            NodeConfiguration node = getNode(configuration, id);
            if (node != null) {
                nodes.put(id, node);
            }
        }
        socketTimeout = configuration.getInt(SOCKET_TIMEOUT_KEY);
        messageRetry = configuration.getInt(MESSAGE_RETRY_KEY);
        messageRetryTimeout = configuration.getInt(MESSAGE_RETRY_TIMEOUT_KEY);
        paxosSlotWindow = configuration.getInt(PAXOS_SLOT_WIDOW_KEY);
        senderPoolSize = configuration.getInt(SENDER_POOL_SIZE_KEY);
        instanceRepeatPoolSize = configuration.getInt(INSTANCE_REPEAT_POOL_SIZE_KEY);
        instanceRepeatTimeout = configuration.getInt(INSTANCE_REPEAT_TIMEOUT_KEY);
    }

    private NodeConfiguration getNode(Configuration configuration, String id) {
        try {
            String address = configuration.getString(NODE_PREFIX + id);
            String[] rolesArray = configuration.getStringArray(ROLES_PREFIX + id);
            Set<Role> roles = parseRoles(rolesArray);
            if (roles == null || roles.isEmpty()) {
                log.warn("Error parsing node with id {}: empty or corrupt role list {}", id, Arrays.toString(rolesArray));
                return null;
            }
            int index = address.indexOf(':');
            if (index >= 0) {
                String host = address.substring(0, index);
                String portString = address.substring(index + 1);
                try {
                    int port = Integer.parseInt(portString);
                    return new NodeConfiguration(id, host, port, roles);
                } catch (NumberFormatException e) {
                    log.warn("Error parsing node with id " + id + ": wrong port " + portString, e);
                    return null;
                }
            } else {
                log.warn("Error parsing node with id {}: wrong address {}", id, address);
                return null;
            }
        } catch (NoSuchElementException | ConversionException e) {
            log.warn("Error parsing node with id " + id, e);
            return null;
        }
    }

    private Set<Role> parseRoles(String[] stringArray) {
        List<Role> roles = new ArrayList<>();
        for (String roleName : stringArray) {
            Role selectedRole = null;
            for (Role role : Role.values()) {
                if (role.name().equalsIgnoreCase(roleName)) {
                    selectedRole = role;
                    break;
                }
            }
            if (selectedRole != null) {
                roles.add(selectedRole);
            } else {
                return null;
            }
        }
        return EnumSet.copyOf(roles);
    }

    public List<Destination> getDestinations(Role role) {
        List<Destination> destinations = new ArrayList<>();
        for (NodeConfiguration node : nodes.values()) {
            if (node.getRoles().contains(role)) {
                destinations.add(new Destination(Destination.Type.NODE, node.getId()));
            }
        }
        return destinations;
    }

}
