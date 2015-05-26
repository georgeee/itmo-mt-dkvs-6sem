package ru.georgeee.itmo.sem6.dkvs.msg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgsMapField {
    Class<? extends Map> container();
    Class key();
    Class value();
}
