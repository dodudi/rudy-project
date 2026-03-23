package com.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DUPLICATE_PAYMENT_CONFIRMATION(HttpStatus.BAD_REQUEST, "PAYMENT_CONFIRM_001", "이미 처리된 주문입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
