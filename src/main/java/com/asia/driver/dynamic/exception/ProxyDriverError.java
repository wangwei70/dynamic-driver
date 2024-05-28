package com.asia.driver.dynamic.exception;

public class ProxyDriverError extends RuntimeException {

    private static final String DEFAULT_MSG="proxy driver error";

    public ProxyDriverError() {
        this(DEFAULT_MSG);
    }

    public ProxyDriverError(String message) {
        super(message);
    }

    public ProxyDriverError(String message, Throwable cause) {
        super(message, cause);
    }
}
