package com.asia.driver.dynamic.exception;

public class ValueFormatError extends RuntimeException {

    private static final String DEFAULT_MSG="form of value error";

    public ValueFormatError() {
        this(DEFAULT_MSG);
    }

    public ValueFormatError(String message) {
        super(message);
    }

    public ValueFormatError(String message, Throwable cause) {
        super(message, cause);
    }
}
