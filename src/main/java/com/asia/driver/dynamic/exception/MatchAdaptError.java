package com.asia.driver.dynamic.exception;

public class MatchAdaptError extends RuntimeException {

    private static final String DEFAULT_MSG="Adatpt error";

    public MatchAdaptError() {
        this(DEFAULT_MSG);
    }

    public MatchAdaptError(String message) {
        super(message);
    }

    public MatchAdaptError( Throwable cause) {
        super(DEFAULT_MSG, cause);
    }

    public MatchAdaptError(String message, Throwable cause) {
        super(message, cause);
    }
}
