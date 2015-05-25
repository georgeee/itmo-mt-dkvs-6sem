package ru.georgeee.itmo.sem6.dkvs.msg;

import java.util.Collection;

public @interface ArgsCollectionField {
    Class<? extends Collection> container();
    Class element();
}
