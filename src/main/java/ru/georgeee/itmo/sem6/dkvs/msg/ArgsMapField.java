package ru.georgeee.itmo.sem6.dkvs.msg;

import java.util.Map;

public @interface ArgsMapField {
    Class<? extends Map> container();
    Class key();
    Class value();
}
