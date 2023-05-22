package com.project.deliveryservice.common.exception;

public class DuplicatedArgumentException extends IllegalArgumentException{
    public DuplicatedArgumentException() {
    }

    public DuplicatedArgumentException(String s) {
        super(s);
    }

    public DuplicatedArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedArgumentException(Throwable cause) {
        super(cause);
    }
}
