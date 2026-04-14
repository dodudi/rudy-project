package com.commerce.exception;

import org.springframework.http.HttpStatus;

public class CommerceException extends RuntimeException {
    private final ErrorCode errorCode;

    public CommerceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    public String getCode() {
        return errorCode.getCode();
    }
}
