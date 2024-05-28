package com.asia.driver.dynamic.exception;

public class JarPathError extends RuntimeException {

    private static final String DEFAULT_MSG="file must be jar package";

    public JarPathError() {
        this(DEFAULT_MSG);
    }

    public JarPathError(String message) {
        super(message);
    }

    public JarPathError(String message, Throwable cause) {
        super(message, cause);
    }
}
