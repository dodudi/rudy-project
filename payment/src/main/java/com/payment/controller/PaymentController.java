package com.payment.controller;

import com.payment.dto.PaymentFilterRequest;
import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.RefundRequest;
import com.payment.dto.TestPaymentRequest;
import com.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Mono<ResponseEntity<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        return paymentService.payment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/test-confirm")
    public Mono<ResponseEntity<PaymentResponse>> testConfirmPayment(
            @Valid @RequestBody TestPaymentRequest request
    ) {
        return paymentService.testPayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refund")
    public Mono<ResponseEntity<PaymentResponse>> refundPayment(
            @Valid @RequestBody RefundRequest request
    ) {
        return paymentService.refund(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<PaymentResponse> getPayments(
            @Valid @ModelAttribute PaymentFilterRequest filter
    ) {
        return paymentService.getPayments(filter);
    }
}
