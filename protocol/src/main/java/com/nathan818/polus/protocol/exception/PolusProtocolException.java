package com.nathan818.polus.protocol.exception;

public class PolusProtocolException extends RuntimeException {
    private static final boolean WITH_STACKTRACE = Boolean.parseBoolean(System.getProperty(
            "com.nathan818.polus.protocol.withStacktrace", "false"));

    public PolusProtocolException() {
    }

    public PolusProtocolException(String message) {
        super(message);
    }

    public PolusProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolusProtocolException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable initCause(Throwable throwable) {
        if (WITH_STACKTRACE) {
            synchronized (this) {
                return super.initCause(throwable);
            }
        }
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        if (WITH_STACKTRACE) {
            synchronized (this) {
                return super.fillInStackTrace();
            }
        }
        return this;
    }
}
