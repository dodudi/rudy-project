package com.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DUPLICATE_PAYMENT_CONFIRMATION(HttpStatus.CONFLICT, "PAYMENT_CONFIRM_001", "이미 처리된 주문입니다"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다"),
    NOT_REFUNDABLE_PAYMENT(HttpStatus.BAD_REQUEST, "PAYMENT_002", "환불 가능한 결제 상태가 아닙니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
