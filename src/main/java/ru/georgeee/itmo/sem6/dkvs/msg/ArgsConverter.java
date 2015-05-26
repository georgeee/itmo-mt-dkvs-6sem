package ru.georgeee.itmo.sem6.dkvs.msg;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.georgeee.itmo.sem6.dkvs.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ArgsConverter {
    /**
     * @throws java.lang.RuntimeException in case of any error
     */
    public static <T extends ArgsConvertible> T parse(Class<T> clazz, String line) {
        return parse(clazz, Utils.splitToArgs(line), 0);
    }

    /**
     * @throws java.lang.RuntimeException in case of any error
     */
    public static <T extends ArgsConvertible> T parse(Class<T> clazz, String[] args) {
        return parse(clazz, args, 0);
    }

    public static <T extends ArgsConvertible> T parse(Class<T> clazz, String[] args, int i) {
        return (T) parseImpl(clazz, args, i).getLeft();
    }

    public static <T extends ArgsConvertible> T parse(Class<T> clazz, Message message) throws MessageParsingException {
        try {
            return parse(clazz, message.getArgs());
        } catch (RuntimeException e) {
            throw new MessageParsingException(e);
        }
    }

    private static <T extends ArgsConvertible> Pair<T, Integer> parseImpl(Class clazz, String[] args, int i) {
        Method method = null;
        try {
            method = clazz.getMethod("parseFromArgs", String[].class, int.class);
            return (Pair<T, Integer>) method.invoke(null, args, i);
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            List<Object> constructorArguments = new ArrayList<>();
            for (Field field : getArgsFields(clazz)) {
                if (field.getType().isPrimitive()) {
                    if (field.getType() == int.class) {
                        constructorArguments.add(Integer.parseInt(args[i++]));
                    } else {
                        throw new UnsupportedOperationException("Doesn't support type: " + field.getType());
                    }
                } else if (field.getType().equals(Integer.class)) {
                    constructorArguments.add(Integer.parseInt(args[i++]));
                } else if (field.getType().equals(String.class)) {
                    constructorArguments.add(args[i++]);
                } else if (Enum.class.isAssignableFrom(field.getType())) {
                    Class<? extends Enum> fieldClass = (Class<? extends Enum>) field.getType();
                    constructorArguments.add(Enum.valueOf(fieldClass, args[i++].toUpperCase()));
                } else if (ArgsConvertible.class.isAssignableFrom(field.getType())) {
                    Pair<ArgsConvertible, Integer> pair = parseImpl(field.getType(), args, i);
                    i = pair.getRight();
                    constructorArguments.add(pair.getLeft());
                } else if (Map.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(ArgsCollectionField.class)) {
                    ArgsMapField mapField = field.getAnnotation(ArgsMapField.class);
                    Class keyType = mapField.key();
                    Class valueType = mapField.value();
                    Map map = mapField.container().newInstance();
                    while (i < args.length) {
                        Pair<ArgsConvertible, Integer> pair1 = parseImpl(keyType, args, i);
                        i = pair1.getRight();
                        Pair<ArgsConvertible, Integer> pair2 = parseImpl(valueType, args, i);
                        i = pair2.getRight();
                        map.put(pair1.getLeft(), pair2.getLeft());
                    }
                    constructorArguments.add(map);
                } else if (Collection.class.isAssignableFrom(field.getType()) && (field.isAnnotationPresent(ArgsCollectionField.class))) {
                    ArgsCollectionField argsCollectionField = field.getAnnotation(ArgsCollectionField.class);
                    Class elementType = argsCollectionField.element();
                    Collection collection = argsCollectionField.container().newInstance();
                    while (i < args.length) {
                        Pair<ArgsConvertible, Integer> pair = parseImpl(elementType, args, i);
                        i = pair.getRight();
                        collection.add(pair.getLeft());
                    }
                    constructorArguments.add(collection);
                } else {
                    throw new UnsupportedOperationException("Doesn't support type: " + field.getType());
                }
            }
            Constructor constructor = getArgConstructor(clazz);
            Object[] arguments = constructorArguments.toArray(new Object[constructorArguments.size()]);
            return new ImmutablePair<>((T) constructor.newInstance(arguments), i);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Constructor getArgConstructor(Class clazz) {
        for (Constructor constructor : clazz.getConstructors()) {
            if (constructor.isAnnotationPresent(ArgsConstructor.class)) {
                return constructor;
            }
        }
        throw new IllegalArgumentException("Should have a constructor with ArgsConstructor annotation: " + clazz);
    }

    private static List<Field> getArgsFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ArgsField.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static Object getValue(ArgsConvertible data, Field field) {
        Class clazz = data.getClass();
        try {
            Method getter = clazz.getMethod("get" + StringUtils.capitalize(field.getName()));
            return getter.invoke(data);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        }
        try {
            return field.get(data);
        } catch (IllegalAccessException e1) {
        }
        return null;
    }

    public static String[] getArgs(ArgsConvertible data) {
        List<Object> args = new ArrayList<>();
        addToArgs(data, args);
        String[] result = new String[args.size()];
        for (int i = 0; i < args.size(); ++i) {
            if (args.get(i) != null) {
                result[i] = args.get(i).toString();
            }
        }
        return result;
    }

    private static void addObjectToArgs(Object object, List<Object> args) {
        if (object instanceof ArgsConvertible) {
            addToArgs((ArgsConvertible) object, args);
        } else if (object instanceof Enum) {
            args.add(((Enum) object).name());
        } else {
            args.add(object);
        }
    }

    private static void addToArgs(ArgsConvertible data, List<Object> args) {
        Class clazz = data.getClass();
        if (data instanceof ArgsConvertibleExtended) {
            ((ArgsConvertibleExtended) data).addToArgs(args);
        }
        for (Field field : getArgsFields(clazz)) {
            if (field.getType().isPrimitive()) {
                if (field.getType() == int.class) {
                    args.add(getValue(data, field));
                } else {
                    throw new UnsupportedOperationException("Doesn't support type: " + field.getType());
                }
            } else if (Enum.class.isAssignableFrom(field.getType())) {
                args.add(((Enum) getValue(data, field)).name());
            } else if (ArgsConvertible.class.isAssignableFrom(field.getType())) {
                addToArgs((ArgsConvertible) getValue(data, field), args);
            } else if (Map.class.isAssignableFrom(field.getType())) {
                if (field.isAnnotationPresent(ArgsCollectionField.class)) {
                    ArgsMapField mapField = field.getAnnotation(ArgsMapField.class);
                    Class keyType = mapField.key();
                    Class valueType = mapField.value();
                    Map map = (Map) getValue(data, field);
                    Set<Map.Entry> entries = map.entrySet();
                    for (Map.Entry entry : entries) {
                        addObjectToArgs(entry.getKey(), args);
                        addObjectToArgs(entry.getValue(), args);
                    }
                }
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                if (field.isAnnotationPresent(ArgsCollectionField.class)) {
                    ArgsCollectionField collectionField = field.getAnnotation(ArgsCollectionField.class);
                    Class elementType = collectionField.element();
                    Collection collection = (Collection) getValue(data, field);
                    for (Object element : collection) {
                        addObjectToArgs(element, args);
                    }
                }
            } else {
                args.add(getValue(data, field));
            }
        }
    }
}
