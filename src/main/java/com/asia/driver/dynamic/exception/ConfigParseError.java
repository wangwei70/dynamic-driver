package com.asia.driver.dynamic.exception;

public class ConfigParseError extends RuntimeException {

    private static final String DEFAULT_MSG="config parse error";

    public ConfigParseError() {
        this(DEFAULT_MSG);
    }

    public ConfigParseError(String message) {
        super(message);
    }

    public ConfigParseError(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigParseError( Throwable cause) {
        super(DEFAULT_MSG, cause);
    }
}
