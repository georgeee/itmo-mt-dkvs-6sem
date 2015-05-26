package ru.georgeee.itmo.sem6.dkvs;

import java.util.Map;
import java.util.regex.Pattern;

public class Utils {
    @SafeVarargs
    public static <K, V> void putBatch(Map<K, V> map, V value, K... keys) {
        for (K key : keys) {
            map.put(key, value);
        }
    }

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    public static boolean containsWhitespace(String s) {
        return WHITESPACE_PATTERN.matcher(s).find();
    }

    public static String[] splitToArgs(String line) {
        return line.trim().split("\\s+");
    }
}
