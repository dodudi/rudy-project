package com.commerce.controller;

import com.commerce.dto.OrderCreateRequest;
import com.commerce.dto.OrderCreateResponse;
import com.commerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(OrderCreateRequest request) {
        OrderCreateResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }
}
