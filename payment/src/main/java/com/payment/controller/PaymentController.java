package com.payment.controller;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Mono<ResponseEntity<PaymentResponse>> createPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.payment(request)
                .map(ResponseEntity::ok);
    }
}
