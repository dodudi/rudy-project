package com.commerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "USER_001", "이미 사용 중인 아이디입니다"),
    DUPLICATE_WALLET(HttpStatus.CONFLICT, "WALLET_001", "이미 잔고가 존재합니다"),
    NOTFOUND_USER(HttpStatus.BAD_REQUEST, "USER_002", "존재하지 않는 사용자입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
