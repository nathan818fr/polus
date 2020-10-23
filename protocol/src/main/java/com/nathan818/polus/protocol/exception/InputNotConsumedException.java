package com.nathan818.polus.protocol.exception;

public class InputNotConsumedException extends PolusProtocolException {
    public InputNotConsumedException(int bytes) {
        super("Received " + bytes + " bytes which were not consumed");
    }
}
