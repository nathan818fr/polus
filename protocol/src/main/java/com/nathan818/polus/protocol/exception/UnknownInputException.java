package com.nathan818.polus.protocol.exception;

public class UnknownInputException extends PolusProtocolException {
    public UnknownInputException(String typeName, Object id) {
        super("Received unknown " + typeName + " with ID " + id);
    }
}
