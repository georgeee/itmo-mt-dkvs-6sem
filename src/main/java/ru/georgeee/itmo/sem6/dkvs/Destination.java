package ru.georgeee.itmo.sem6.dkvs;

import lombok.Getter;
import ru.georgeee.itmo.sem6.dkvs.msg.ArgsField;

public class Destination {
    @Getter @ArgsField
    private final Type type;
    @Getter @ArgsField
    private final String id;

    public Destination(Type type, String id) {
        if (type == null || id == null) {
            throw new IllegalArgumentException("Neither type nor id can be null for destination");
        }
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Destination that = (Destination) o;

        if (!id.equals(that.id)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{" + type +
                ", id='" + id + '\'' +
                '}';
    }

    public enum Type {
        CLIENT, NODE
    }
}
