package com.commerce.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Builder
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "INTEGER CHECK (price >= 1000 AND price <= 100000)")
    private int price;

    @Column(columnDefinition = "INTEGER CHECK (stock >= 0 AND stock <= 10000)")
    private int stock;

    @Column(updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(updatable = true)
    @LastModifiedDate
    private Instant updatedAt;

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고 부족: " + this.name);
        }
        this.stock -= quantity;
    }
}
