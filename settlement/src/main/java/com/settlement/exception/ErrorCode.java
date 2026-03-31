package com.settlement.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    NOTFOUND_SETTLEMENT(HttpStatus.NOT_FOUND, "SETTLEMENT_001", "정산 정보를 찾을 수 없습니다."),
    ALREADY_SETTLED(HttpStatus.CONFLICT, "SETTLEMENT_002", "이미 정산된 주문입니다."),
    INVALID_SETTLEMENT_STATUS(HttpStatus.BAD_REQUEST, "SETTLEMENT_003", "처리할 수 없는 정산 상태입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
