package com.settlement.domain;

import com.settlement.exception.ErrorCode;
import com.settlement.exception.SettlementException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Entity
@Getter
@Table(name = "settlement_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SettlementRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String paymentKey;

    @Column(nullable = false)
    private int grossAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Column(nullable = false)
    private Instant paymentCompletedAt;

    @Column(nullable = false)
    private LocalDate settlementDate;

    private Instant settledAt;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private SettlementRecord(Long orderId, Long sellerId, String paymentKey, int grossAmount, Instant paymentCompletedAt) {
        Assert.notNull(orderId, "주문 ID는 필수 값입니다.");
        Assert.notNull(sellerId, "판매자 ID는 필수 값입니다.");
        Assert.hasText(paymentKey, "결제 키는 필수 값입니다.");
        Assert.isTrue(grossAmount > 0, "결제 금액은 0보다 커야 합니다.");
        Assert.notNull(paymentCompletedAt, "결제 완료 시각은 필수 값입니다.");

        this.orderId = orderId;
        this.sellerId = sellerId;
        this.paymentKey = paymentKey;
        this.grossAmount = grossAmount;
        this.paymentCompletedAt = paymentCompletedAt;
        this.settlementDate = paymentCompletedAt.atZone(ZoneOffset.UTC).toLocalDate();
        this.status = SettlementStatus.PENDING;
    }

    public void settle() {
        if (this.status != SettlementStatus.PENDING) {
            throw new SettlementException(ErrorCode.INVALID_SETTLEMENT_STATUS);
        }
        this.status = SettlementStatus.SETTLED;
        this.settledAt = Instant.now();
    }

    public void cancel() {
        if (this.status != SettlementStatus.PENDING) {
            throw new SettlementException(ErrorCode.INVALID_SETTLEMENT_STATUS);
        }
        this.status = SettlementStatus.CANCELLED;
    }
}
