package ru.georgeee.itmo.sem6.dkvs.msg;


import java.util.List;

public interface ArgsConvertibleExtended extends ArgsConvertible {
    void addToArgs(List<Object> args);
}
