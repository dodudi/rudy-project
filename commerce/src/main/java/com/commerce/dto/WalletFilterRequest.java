package com.commerce.dto;

public record WalletFilterRequest(
        Long memberId,
        Boolean hasBalance
) {
}
