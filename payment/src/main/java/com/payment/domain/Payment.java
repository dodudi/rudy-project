package com.payment.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

import java.time.Instant;

@Getter
@Table("payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    private Long id;

    private Long orderId;

    private String paymentId;

    private String paymentKey;

    private int amount;

    private String status;

    @CreatedDate
    private Instant createdAt;

    @Builder
    private Payment(Long orderId, String paymentKey, String paymentId, int amount) {
        Assert.notNull(orderId, "Payment 주문ID는 필수 값입니다.");
        Assert.notNull(paymentId, "Payment 토스주문ID는 필수 값입니다.");
        Assert.hasText(paymentKey, "Payment 토스주문KEY는 필수 값입니다.");
        Assert.isTrue(amount >= 0, "구매금액은 0 이상이어야 합니다.");

        this.orderId = orderId;
        this.amount = amount;
        this.paymentId = paymentId;
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.PENDING.name();
        this.createdAt = Instant.now();
    }

    public Payment markCompleted() {
        this.status = PaymentStatus.COMPLETED.name();
        return this;
    }

    public Payment markFailed() {
        this.status = PaymentStatus.FAILED.name();
        return this;
    }

    public Payment markRefunded() {
        this.status = PaymentStatus.REFUNDED.name();
        return this;
    }

    public boolean isRefundable() {
        return PaymentStatus.COMPLETED.name().equals(this.status);
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}
