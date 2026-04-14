package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Wallet;
import com.commerce.dto.WalletCreateRequest;
import com.commerce.dto.WalletFilterRequest;
import com.commerce.dto.WalletResponse;
import com.commerce.dto.WalletTransactionRequest;
import com.commerce.exception.DuplicateException;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.WalletRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@SpringBootTest
class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .username("username")
                .password("@testpassword1234")
                .nickname("nickname")
                .build());
    }

    @Test
    void 잔고생성_성공() {
        // given
        Long memberId = member.getId();
        long balance = 100000000;

        WalletCreateRequest request = new WalletCreateRequest(memberId, balance);

        // when
        WalletResponse wallet = walletService.createWallet(request);

        // then
        Assertions.assertThat(wallet.balance()).isEqualTo(balance);
        Assertions.assertThat(wallet.nickname()).isEqualTo(member.getNickname());
    }

    @Test
    void 잔고생성_중복_에러발생() {
        // given
        Long memberId = member.getId();
        long balance = 100000000;

        walletRepository.save(Wallet.builder()
                .member(member)
                .balance(balance)
                .build()
        );

        WalletCreateRequest request = new WalletCreateRequest(memberId, balance);

        // when
        Assertions.assertThatThrownBy(() -> walletService.createWallet(request))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("이미 잔고가 존재합니다");
    }

    @Test
    void 잔고생성_실패_존재하지_않는_회원() {
        // given
        Long invalidMemberId = 3333L;
        WalletCreateRequest request = new WalletCreateRequest(invalidMemberId, 1000L);

        // when & then
        Assertions.assertThatThrownBy(() -> walletService.createWallet(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다");
    }

    @Test
    void 잔고생성_실패_최소금액_미달() {
        // given
        Long memberId = member.getId();
        long negativeBalance = -1000L;
        WalletCreateRequest request = new WalletCreateRequest(memberId, negativeBalance);

        // when & then
        Assertions.assertThatThrownBy(() -> walletService.createWallet(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔고 금액은 0원 이상");
    }

    @Test
    void 잔고생성_실패_최대금액_초과() {
        // given
        Long memberId = member.getId();
        long overBalance = 100_000_001L;
        WalletCreateRequest request = new WalletCreateRequest(memberId, overBalance);

        // when & then
        Assertions.assertThatThrownBy(() -> walletService.createWallet(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1억 원 이하만 가능합니다");
    }

    @Test
    void 잔고조회_성공_잔액있는_지갑만_반환() {
        // given
        Member member2 = memberRepository.save(Member.builder()
                .username("username2").password("@testpassword1234").nickname("nickname2").build());

        walletRepository.save(Wallet.builder().member(member).balance(10000L).build());
        walletRepository.save(Wallet.builder().member(member2).balance(0L).build());

        WalletFilterRequest filter = new WalletFilterRequest(null, true);

        // when
        List<WalletResponse> result = walletService.getWallets(filter);

        // then
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result).allMatch(w -> w.balance() > 0);
    }

    @Test
    void 잔고조회_성공_잔액없는_지갑만_반환() {
        // given
        Member member2 = memberRepository.save(Member.builder()
                .username("username2").password("@testpassword1234").nickname("nickname2").build());

        walletRepository.save(Wallet.builder().member(member).balance(10000L).build());
        walletRepository.save(Wallet.builder().member(member2).balance(0L).build());

        WalletFilterRequest filter = new WalletFilterRequest(null, false);

        // when
        List<WalletResponse> result = walletService.getWallets(filter);

        // then
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result).allMatch(w -> w.balance() == 0);
    }

    @Test
    void 잔고조회_성공_전체_반환() {
        // given
        Member member2 = memberRepository.save(Member.builder()
                .username("username2").password("@testpassword1234").nickname("nickname2").build());

        walletRepository.save(Wallet.builder().member(member).balance(10000L).build());
        walletRepository.save(Wallet.builder().member(member2).balance(0L).build());

        WalletFilterRequest filter = new WalletFilterRequest(null, null);

        // when
        List<WalletResponse> result = walletService.getWallets(filter);

        // then
        Assertions.assertThat(result).hasSize(2);
    }

    @Test
    void 입금_성공() {
        // given
        Wallet wallet = walletRepository.save(Wallet.builder().member(member).balance(10000L).build());
        WalletTransactionRequest request = new WalletTransactionRequest(5000L);

        // when
        WalletResponse result = walletService.deposit(wallet.getId(), request);

        // then
        Assertions.assertThat(result.balance()).isEqualTo(15000L);
    }

    @Test
    void 입금_실패_한도초과() {
        // given
        Wallet wallet = walletRepository.save(Wallet.builder().member(member).balance(99_000_000L).build());
        WalletTransactionRequest request = new WalletTransactionRequest(2_000_000L);

        // when & then
        Assertions.assertThatThrownBy(() -> walletService.deposit(wallet.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1억 원을 초과할 수 없습니다");
    }

    @Test
    void 입금_실패_존재하지_않는_잔고() {
        // given
        WalletTransactionRequest request = new WalletTransactionRequest(5000L);

        // when & then
        Assertions.assertThatThrownBy(() -> walletService.deposit(9999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 잔고입니다");
    }

    @Test
    void 출금_성공() {
        // given
        Wallet wallet = walletRepository.save(Wallet.builder().member(member).balance(10000L).build());
        WalletTransactionRequest request = new WalletTransactionRequest(3000L);

        // when
        WalletResponse result = walletService.withdraw(wallet.getId(), request);

        // then
        Assertions.assertThat(result.balance()).isEqualTo(7000L);
    }

    @Test
    void 출금_실패_잔고_부족() {
        // given
        Wallet wallet = walletRepository.save(Wallet.builder().member(member).balance(1000L).build());
        WalletTransactionRequest request = new WalletTransactionRequest(5000L);

        // when & then
        Assertions.assertThatThrownBy(() -> walletService.withdraw(wallet.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잔고가 부족합니다");
    }

    @Test
    void 출금_실패_존재하지_않는_잔고() {
        // given
        WalletTransactionRequest request = new WalletTransactionRequest(5000L);

        // when & then
        Assertions.assertThatThrownBy(() -> walletService.withdraw(9999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 잔고입니다");
    }

}