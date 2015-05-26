package ru.georgeee.itmo.sem6.dkvs.msg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgsCollectionField {
    Class<? extends Collection> container();
    Class element();
}
