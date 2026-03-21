package com.commerce.controller;

import com.commerce.dto.OrderCreateRequest;
import com.commerce.dto.OrderCreateResponse;
import com.commerce.dto.OrderFilterRequest;
import com.commerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderCreateResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderCreateResponse>> getOrders(@ModelAttribute OrderFilterRequest filter) {
        return ResponseEntity.ok(orderService.getOrders(filter));
    }
}
