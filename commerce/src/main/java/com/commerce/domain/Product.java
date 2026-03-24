package com.commerce.domain;

import com.commerce.exception.ErrorCode;
import com.commerce.exception.InsufficientStockException;
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
@Table(name = "products")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "INTEGER CHECK (price >= 0 AND price <= 100000000)")
    private int price;

    @Column(columnDefinition = "INTEGER CHECK (stock >= 0 AND stock <= 10000000)")
    private int stock;

    @Column(updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(updatable = true)
    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private Product(Member member, String name, String description, int price, int stock) {
        Assert.notNull(member, "회원 정보는 필수 값입니다.");
        Assert.hasText(name, "상품 이름은 필수 입력 값입니다.");
        Assert.isTrue(name.length() >= 2 && name.length() <= 20, "상품 이름은 2자 이상 20자 이하로 입력해야 합니다.");
        Assert.isTrue(name.matches("^[a-zA-Z0-9가-힣]*$"), "상품 이름에 특수문자를 포함할 수 없습니다.");
        Assert.isTrue(description.length() <= 500, "상품 설명은 최대 500자까지 입력 가능합니다.");
        Assert.isTrue(price >= 0 && price <= 100_000_000, "상품 가격은 0원 이상 1억 원 이하만 가능합니다.");
        Assert.isTrue(stock >= 0 && stock <= 10_000_000, "재고 수량은 0개 이상 1,000만 개 이하만 가능합니다.");

        this.member = member;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void update(String name, String description, Integer price, Integer stock) {
        if (name != null) {
            Assert.hasText(name, "상품 이름은 필수 입력 값입니다.");
            Assert.isTrue(name.length() >= 2 && name.length() <= 20, "상품 이름은 2자 이상 20자 이하로 입력해야 합니다.");
            Assert.isTrue(name.matches("^[a-zA-Z0-9가-힣]*$"), "상품 이름에 특수문자를 포함할 수 없습니다.");
            this.name = name;
        }

        if (description != null) {
            Assert.isTrue(description.length() <= 500, "상품 설명은 최대 500자까지 입력 가능합니다.");
            this.description = description;
        }

        if (price != null) {
            Assert.isTrue(price >= 0 && price <= 100_000_000, "상품 가격은 0원 이상 1억 원 이하만 가능합니다.");
            this.price = price;
        }

        if (stock != null) {
            Assert.isTrue(stock >= 0 && stock <= 10_000_000, "재고 수량은 0개 이상 1,000만 개 이하만 가능합니다.");
            this.stock = stock;
        }
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new InsufficientStockException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

}
