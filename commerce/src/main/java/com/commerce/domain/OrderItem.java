package com.commerce.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Getter
@Table(name = "order_items")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
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

    public static OrderItem create(Order order, Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.order = order;
        item.product = product;
        item.price = product.getPrice();
        item.quantity = quantity;
        item.amount = item.price * item.quantity;
        return item;
    }
}
