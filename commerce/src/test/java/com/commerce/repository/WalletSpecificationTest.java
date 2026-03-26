package com.commerce.repository;

import com.commerce.domain.Member;
import com.commerce.domain.Wallet;
import com.commerce.dto.WalletFilterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class WalletSpecificationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(Member.builder()
                .username("user1").password("@password1234").nickname("nick1").build());
        member2 = memberRepository.save(Member.builder()
                .username("user2").password("@password1234").nickname("nick2").build());
        member3 = memberRepository.save(Member.builder()
                .username("user3").password("@password1234").nickname("nick3").build());

        walletRepository.save(Wallet.builder().member(member1).balance(10000L).build());  // 잔액 있음
        walletRepository.save(Wallet.builder().member(member2).balance(50000L).build());  // 잔액 있음
        walletRepository.save(Wallet.builder().member(member3).balance(0L).build());      // 잔액 없음
    }

    @Test
    void hasBalance_true_잔액있는_지갑만_조회() {
        // given
        WalletFilterRequest filter = new WalletFilterRequest(null, true);

        // when
        List<Wallet> result = walletRepository.findAll(WalletSpecification.withFilter(filter));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(w -> w.getBalance() > 0);
    }

    @Test
    void hasBalance_false_잔액없는_지갑만_조회() {
        // given
        WalletFilterRequest filter = new WalletFilterRequest(null, false);

        // when
        List<Wallet> result = walletRepository.findAll(WalletSpecification.withFilter(filter));

        // then
        assertThat(result).hasSize(1);
        assertThat(result).allMatch(w -> w.getBalance() == 0);
    }

    @Test
    void hasBalance_null_전체_조회() {
        // given
        WalletFilterRequest filter = new WalletFilterRequest(null, null);

        // when
        List<Wallet> result = walletRepository.findAll(WalletSpecification.withFilter(filter));

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    void memberId_필터_특정_회원_지갑만_조회() {
        // given
        WalletFilterRequest filter = new WalletFilterRequest(member1.getId(), null);

        // when
        List<Wallet> result = walletRepository.findAll(WalletSpecification.withFilter(filter));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMember().getId()).isEqualTo(member1.getId());
    }

    @Test
    void memberId_와_hasBalance_복합_필터_조회() {
        // given - member1은 잔액 있음, 이 조건으로 조회
        WalletFilterRequest filter = new WalletFilterRequest(member1.getId(), true);

        // when
        List<Wallet> result = walletRepository.findAll(WalletSpecification.withFilter(filter));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMember().getId()).isEqualTo(member1.getId());
        assertThat(result.get(0).getBalance()).isGreaterThan(0);
    }

    @Test
    void memberId_와_hasBalance_복합_필터_조회_결과없음() {
        // given - member3은 잔액 없음, hasBalance=true 조건으로 조회 시 결과 없어야 함
        WalletFilterRequest filter = new WalletFilterRequest(member3.getId(), true);

        // when
        List<Wallet> result = walletRepository.findAll(WalletSpecification.withFilter(filter));

        // then
        assertThat(result).isEmpty();
    }
}
