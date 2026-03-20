package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Wallet;
import com.commerce.dto.WalletCreateRequest;
import com.commerce.dto.WalletResponse;
import com.commerce.exception.DuplicateException;
import com.commerce.exception.ErrorCode;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public WalletResponse createWallet(WalletCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_USER));

        if (walletRepository.existsByMemberId(request.memberId())) {
            throw new DuplicateException(ErrorCode.DUPLICATE_WALLET);
        }

        Wallet wallet = walletRepository.save(Wallet.builder()
                .member(member)
                .balance(request.balance())
                .build());

        return WalletResponse.from(wallet);
    }
}
