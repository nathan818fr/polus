package com.nathan818.polus.protocol.exception;

public class InputOverflowException extends PolusProtocolException {
    public InputOverflowException(String message) {
        super(message);
    }

    public InputOverflowException(String typeName, int bytes, int maxBytes) {
        this(typeName, bytes, maxBytes, "bytes");
    }

    public InputOverflowException(String typeName, int size, int maxSize, String sizeUnit) {
        super("Cannot receive " + typeName + " longer than " + size + " (got " + maxSize + " " + sizeUnit + ")");
    }
}
