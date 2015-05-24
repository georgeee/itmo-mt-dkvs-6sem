package ru.georgeee.itmo.sem6.dkvs;

import java.util.Map;

public class Utils {
    @SafeVarargs
    public static <K, V> void putBatch(Map<K, V> map, V value, K... keys) {
        for (K key : keys) {
            map.put(key, value);
        }
    }

}
