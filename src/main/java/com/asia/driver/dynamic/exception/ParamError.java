package com.asia.driver.dynamic.exception;

public class ParamError extends RuntimeException {

    private static final String DEFAULT_MSG="param error";

    public ParamError() {
        this(DEFAULT_MSG);
    }

    public ParamError(String msg) {
        super(msg);
    }

    public ParamError(String message, Throwable cause) {
        super(message, cause);
    }
}
