package com.settlement.exception;

import org.springframework.http.HttpStatus;

public class SettlementException extends RuntimeException {

    private final ErrorCode errorCode;

    public SettlementException(ErrorCode errorCode) {
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
