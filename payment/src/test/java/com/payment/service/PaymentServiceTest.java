package com.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.config.TossPaymentClient;
import com.payment.domain.Outbox;
import com.payment.domain.Payment;
import com.payment.dto.PaymentRequest;
import com.payment.dto.TossConfirmResponse;
import com.payment.repository.OutboxRepository;
import com.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.payment.exception.ErrorCode;
import com.payment.exception.PaymentException;
import com.payment.exception.TossPaymentException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock OutboxRepository outboxRepository;
    @Mock TossPaymentClient tossPaymentClient;
    @Mock ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    @Mock ReactiveValueOperations<String, String> valueOperations;

    PaymentService paymentService;

    @BeforeAll
    static void installBlockHound() {
        BlockHound.install();
    }

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository, outboxRepository, tossPaymentClient,
                new ObjectMapper(), reactiveRedisTemplate
        );
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 결제승인_블록코드_없음성공() {
        // given
        PaymentRequest request = new PaymentRequest("paymentKey", 1L, "tossOrderId", 10000);
        when(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(Mono.just(true));
        when(tossPaymentClient.confirm(any())).thenReturn(
                Mono.just(new TossConfirmResponse("paymentKey", "tossOrderId", "DONE", 10000))
        );
        when(paymentRepository.save(any())).thenReturn(
                Mono.just(Payment.builder().orderId(1L).paymentKey("paymentKey").paymentId("tossOrderId").amount(10000).build().markCompleted())
        );
        when(outboxRepository.save(any())).thenReturn(
                Mono.just(Outbox.builder().topic("payment.result").payload("{}").build())
        );

        // when & then
        StepVerifier.create(paymentService.payment(request))
                .expectNextMatches(response -> response.status().equals("COMPLETED"))
                .verifyComplete();
    }

    @Test
    void 결제승인_토스실패_에러처리() {
        // given
        PaymentRequest request = new PaymentRequest("paymentKey", 1L, "tossOrderId", 10000);
        when(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(Mono.just(true));
        when(tossPaymentClient.confirm(any())).thenReturn(Mono.error(new TossPaymentException("잔액 부족")));
        when(paymentRepository.save(any())).thenReturn(
                Mono.just(Payment.builder().orderId(1L).paymentKey("paymentKey").paymentId("tossOrderId").amount(10000).build().markFailed())
        );
        when(outboxRepository.save(any())).thenReturn(
                Mono.just(Outbox.builder().topic("payment.result").payload("{}").build())
        );
        when(reactiveRedisTemplate.delete(any(String.class))).thenReturn(Mono.just(1L));

        // when & then
        StepVerifier.create(paymentService.payment(request))
                .expectNextMatches(response -> response.status().equals("FAILED"))
                .verifyComplete();
        verify(outboxRepository).save(any());
    }

    @Test
    void 결제승인_중복요청_예외발생() {
        // given
        PaymentRequest request = new PaymentRequest("paymentKey", 1L, "tossOrderId", 10000);
        when(valueOperations.setIfAbsent(any(), any(), any())).thenReturn(Mono.just(false));

        // when & then
        StepVerifier.create(paymentService.payment(request))
                .expectErrorMatches(e -> e instanceof PaymentException &&
                        e.getMessage().equals(ErrorCode.DUPLICATE_PAYMENT_CONFIRMATION.getMessage()))
                .verify();
    }
}
