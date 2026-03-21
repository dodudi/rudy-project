package com.commerce.domain;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(updatable = true)
    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private Order(Member member) {
        Assert.notNull(member, "회원 정보는 필수 값입니다.");

        this.member = member;
        this.status = OrderStatus.CREATED;
    }

    public void addOrderItem(Product product, int quantity) {
        OrderItem item = OrderItem.builder()
                .order(this)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .build();

        orderItems.add(item);
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public int getTotalAmount() {
        return orderItems.stream()
                .mapToInt(OrderItem::getAmount)
                .sum();
    }
}
