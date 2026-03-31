package com.settlement.domain;

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

@Entity
@Getter
@Table(
        name = "daily_settlements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"seller_id", "settlement_date"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DailySettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private int orderCount;

    @Column(nullable = false)
    private long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DailySettlementStatus status;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private DailySettlement(Long sellerId, LocalDate settlementDate, int orderCount, long totalAmount) {
        Assert.notNull(sellerId, "판매자 ID는 필수 값입니다.");
        Assert.notNull(settlementDate, "정산 기준일은 필수 값입니다.");
        Assert.isTrue(orderCount > 0, "주문 수는 0보다 커야 합니다.");
        Assert.isTrue(totalAmount > 0, "정산 총액은 0보다 커야 합니다.");

        this.sellerId = sellerId;
        this.settlementDate = settlementDate;
        this.orderCount = orderCount;
        this.totalAmount = totalAmount;
        this.status = DailySettlementStatus.COMPLETED;
    }
}
