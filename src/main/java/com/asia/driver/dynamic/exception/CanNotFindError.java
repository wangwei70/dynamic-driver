package com.asia.driver.dynamic.exception;

public class CanNotFindError extends RuntimeException {

    private static final String DEFAULT_MSG="can not match driver";

    public CanNotFindError() {
        this(DEFAULT_MSG);
    }

    public CanNotFindError(String driver) {
        super(DEFAULT_MSG+":"+driver);
    }

    public CanNotFindError(String message, Throwable cause) {
        super(message, cause);
    }
}
