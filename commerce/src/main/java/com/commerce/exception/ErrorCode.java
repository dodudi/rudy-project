package com.commerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "USER_001", "이미 사용 중인 아이디입니다"),
    DUPLICATE_WALLET(HttpStatus.CONFLICT, "WALLET_001", "이미 잔고가 존재합니다"),
    NOTFOUND_USER(HttpStatus.BAD_REQUEST, "USER_002", "존재하지 않는 사용자입니다"),

    DUPLICATE_PRODUCT_NAME(HttpStatus.CONFLICT, "PRODUCT_001", "이미 사용 중인 상품 이름입니다"),
    NOTFOUND_PRODUCT(HttpStatus.BAD_REQUEST, "PRODUCT_002", "존재하지 않는 상품입니다"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_003", "재고가 부족합니다"),
    EMPTY_PRODUCT(HttpStatus.BAD_REQUEST, "PRODUCT_004", "상품 정보가 비었습니다"),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "PRODUCT_005", "최소 가격은 최대 가격보다 클 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
