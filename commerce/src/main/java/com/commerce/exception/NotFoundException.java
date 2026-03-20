package com.commerce.exception;

public class NotFoundException extends CommerceException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
