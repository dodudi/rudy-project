package com.commerce.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreateRequest {
    private final Long memberId;
    private final List<OrderItem> orderItems;

    public OrderCreateRequest(Long memberId, List<OrderItem> orderItems) {
        this.memberId = memberId;
        this.orderItems = orderItems;
    }

    public record OrderItem(Long productId, int quantity) {
    }

}
