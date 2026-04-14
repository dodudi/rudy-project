package com.commerce.dto;

import com.commerce.domain.Wallet;

public record WalletResponse(
        Long id,
        String nickname,
        Long balance
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getMember().getNickname(), wallet.getBalance());
    }
}
