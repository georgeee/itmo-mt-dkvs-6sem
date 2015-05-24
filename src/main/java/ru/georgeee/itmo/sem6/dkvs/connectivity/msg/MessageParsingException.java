package ru.georgeee.itmo.sem6.dkvs.connectivity.msg;

import java.util.Arrays;

public class MessageParsingException extends Exception {
    public MessageParsingException() {
    }

    public MessageParsingException(String message) {
        super(message);
    }

    public MessageParsingException(String[] args) {
        super("Error parsing from args: " + Arrays.toString(args));
    }

    public MessageParsingException(String[] args, Throwable cause) {
        super("Error parsing from args: " + Arrays.toString(args), cause);
    }

    public MessageParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageParsingException(Throwable cause) {
        super(cause);
    }
}
