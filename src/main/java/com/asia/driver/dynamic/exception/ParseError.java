package com.asia.driver.dynamic.exception;

public class ParseError extends RuntimeException {

    private static final String DEFAULT_MSG="parse config error";

    public ParseError() {
        this(DEFAULT_MSG);
    }

    public ParseError(String message) {
        super(message);
    }

    public ParseError( Throwable cause) {
        super(DEFAULT_MSG, cause);
    }

    public ParseError(String message, Throwable cause) {
        super(message, cause);
    }
}
