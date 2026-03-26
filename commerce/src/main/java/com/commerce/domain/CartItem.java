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
@Table(name = "cart_items")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CartItem {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    private int amount;

    private int price;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private CartItem(Cart cart, Product product, int quantity, int price) {
        Assert.notNull(cart, "장바구니 정보는 필수 값입니다.");
        Assert.notNull(product, "상품 정보는 필수 값입니다.");
        Assert.isTrue(quantity >= 1, "수량은 1개 이상이어야 합니다.");
        Assert.isTrue(price >= 0, "상품 가격은 0원 이상이어야 합니다.");

        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.amount = quantity * price;
    }
}
