package com.commerce.ui.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrderRequest {

    @NotNull(message = "회원 ID를 입력해주세요")
    private Long memberId;

    @Valid
    @NotEmpty(message = "주문 상품을 1개 이상 추가해주세요")
    private List<OrderItemRequest> orderItems = new ArrayList<>();

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public List<OrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }
}
