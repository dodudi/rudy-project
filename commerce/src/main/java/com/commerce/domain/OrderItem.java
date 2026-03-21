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
@Table(name = "order_items")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int price;

    private int quantity;

    private int amount;

    @Column(updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(updatable = true)
    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private OrderItem(Order order, Product product, int price, int quantity) {
        Assert.notNull(order, "주문 정보는 필수 값입니다.");
        Assert.notNull(product, "상품 정보는 필수 값입니다.");
        Assert.isTrue(price >= 0, "상품 가격은 0원 이상이어야 합니다.");
        Assert.isTrue(quantity >= 1, "상품 구매 수량은 1개 이상이어야 합니다.");

        this.order = order;
        this.product = product;
        this.price = price;
        this.quantity = quantity;
        this.amount = this.price * this.quantity;
    }
}
