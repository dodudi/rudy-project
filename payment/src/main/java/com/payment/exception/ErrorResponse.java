package com.payment.exception;

import java.time.Instant;

public record ErrorResponse(String code, String message, Instant timestamp) {
}
