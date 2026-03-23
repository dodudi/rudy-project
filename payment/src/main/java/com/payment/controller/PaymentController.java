package com.payment.controller;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.RefundRequest;
import com.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/refund")
    public Mono<ResponseEntity<PaymentResponse>> refundPayment(
            @Valid @RequestBody RefundRequest request
    ) {
        return paymentService.refund(request)
                .map(ResponseEntity::ok);
    }
}
