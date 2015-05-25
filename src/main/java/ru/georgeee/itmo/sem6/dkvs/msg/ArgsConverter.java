package ru.georgeee.itmo.sem6.dkvs.msg;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArgsConverter {
    /**
     * @throws java.lang.RuntimeException in case of any error
     */
    public static <T extends ArgsConvertible> T parse(Class<T> clazz, String[] args) {
        return (T) parseImpl(clazz, args, 0).getLeft();
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
                } else if (field.getType().equals(String.class)) {
                    constructorArguments.add(args[i++]);
                } else if (ArgsConvertible.class.isAssignableFrom(field.getType())) {
                    Pair<ArgsConvertible, Integer> pair = parseImpl(field.getType(), args, i);
                    i = pair.getRight();
                    constructorArguments.add(pair.getLeft());
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    if (field.isAnnotationPresent(ArgsCollectionField.class)) {
                        ArgsCollectionField argsCollectionField = field.getAnnotation(ArgsCollectionField.class);
                        Class elementType = argsCollectionField.element();
                        Collection collection = argsCollectionField.container().newInstance();
                        while (i < args.length) {
                            Pair<ArgsConvertible, Integer> pair = parseImpl(elementType, args, i);
                            i = pair.getRight();
                            collection.add(pair.getLeft());
                        }
                        constructorArguments.add(collection);
                    }
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
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(ArgsField.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static Object getValue(ArgsConvertible data, Field field) {
        Class clazz = data.getClass();
        try {
            Method getter = clazz.getMethod("get" + field.getName());
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
            result[i] = args.get(i).toString();
        }
        return result;
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
            } else if (ArgsConvertible.class.isAssignableFrom(field.getType())) {
                addToArgs((ArgsConvertible) getValue(data, field), args);
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                if (field.isAnnotationPresent(ArgsCollectionField.class)) {
                    ArgsCollectionField argsCollectionField = field.getAnnotation(ArgsCollectionField.class);
                    Class elementType = argsCollectionField.element();
                    Collection collection = (Collection) getValue(data, field);
                    for (Object element : collection) {
                        if (ArgsConvertible.class.isAssignableFrom(elementType)) {
                            addToArgs((ArgsConvertible) element, args);
                        } else {
                            args.add(element);
                        }
                    }
                }
            } else {
                args.add(getValue(data, field));
            }
        }
    }
}
