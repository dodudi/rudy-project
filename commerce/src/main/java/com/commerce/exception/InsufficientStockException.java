package com.commerce.exception;

public class InsufficientStockException extends CommerceException {
    public InsufficientStockException(ErrorCode errorCode) {
        super(errorCode);
    }
}
