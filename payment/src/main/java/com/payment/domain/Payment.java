package com.payment.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("payments")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    private Long id;

    private Long orderId;

    private Long memberId;

    private int amount;

    private String status;

    @CreatedDate
    private Instant createdAt;

    public static Payment create(Long orderId, Long memberId, int amount) {
        return Payment.builder()
                .orderId(orderId)
                .memberId(memberId)
                .amount(amount)
                .status(PaymentStatus.PENDING.name())
                .build();
    }

    public Payment withStatus(PaymentStatus newStatus) {
        return Payment.builder()
                .id(this.id)
                .orderId(this.orderId)
                .memberId(this.memberId)
                .amount(this.amount)
                .status(newStatus.name())
                .createdAt(this.createdAt)
                .build();
    }
}
