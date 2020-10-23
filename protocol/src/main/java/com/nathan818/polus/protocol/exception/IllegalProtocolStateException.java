package com.nathan818.polus.protocol.exception;

public class IllegalProtocolStateException extends PolusProtocolException {
    public IllegalProtocolStateException(String message) {
        super(message);
    }

    public IllegalProtocolStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
