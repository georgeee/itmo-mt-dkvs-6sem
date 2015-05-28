package ru.georgeee.itmo.sem6.dkvs.controller;

import ru.georgeee.itmo.sem6.dkvs.Destination;

public interface Controller {

    void start();

    void stop();

    String ping(Destination destination, Runnable onPong);

    void ping(Destination destination, String token, Runnable onPong);

    void removePingToken(Destination destination, String token);
}
