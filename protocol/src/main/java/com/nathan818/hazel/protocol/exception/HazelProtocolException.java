package com.nathan818.hazel.protocol.exception;

public class HazelProtocolException extends RuntimeException {
    private static final boolean WITH_STACKTRACE = Boolean.parseBoolean(System.getProperty("com.nathan818.hazel.protocol.withStacktrace", "false"));

    public HazelProtocolException() {
    }

    public HazelProtocolException(String message) {
        super(message);
    }

    public HazelProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public HazelProtocolException(Throwable cause) {
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
