package com.commerce.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

import java.time.Instant;

@Entity
@Getter
@Table(name = "wallets")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(columnDefinition = "INTEGER CHECK (balance >= 0)")
    private Long balance;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = true)
    private Instant updatedAt;

    @Builder
    private Wallet(Member member, Long balance) {
        Assert.notNull(member, "회원 정보는 필수 값입니다.");
        Assert.isTrue(balance >= 0 && balance <= 100_000_000, "잔고 금액은 0원 이상 1억 원 이하만 가능합니다.");

        this.member = member;
        this.balance = balance;
    }

    public void deposit(long amount) {
        Assert.isTrue(amount > 0, "입금 금액은 0원 초과이어야 합니다.");
        Assert.isTrue(this.balance + amount <= 100_000_000, "잔고는 1억 원을 초과할 수 없습니다.");
        this.balance += amount;
    }

    public void withdraw(long amount) {
        Assert.isTrue(amount > 0, "출금 금액은 0원 초과이어야 합니다.");
        Assert.isTrue(this.balance >= amount, "잔고가 부족합니다.");
        this.balance -= amount;
    }
}
