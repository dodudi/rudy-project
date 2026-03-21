package com.commerce.exception;

public class EmptyException extends CommerceException {
    public EmptyException(ErrorCode errorCode) {
        super(errorCode);
    }
}
