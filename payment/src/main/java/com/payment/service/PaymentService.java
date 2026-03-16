package com.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.domain.Outbox;
import com.payment.domain.Payment;
import com.payment.domain.PaymentStatus;
import com.payment.dto.PaymentResponse;
import com.payment.event.PaymentRequestEvent;
import com.payment.event.PaymentResultEvent;
import com.payment.repository.OutboxRepository;
import com.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final TransactionalOperator transactionalOperator;
    private final ObjectMapper objectMapper;

    public Flux<PaymentResponse> getPayments() {
        return paymentRepository.findAll()
                .map(PaymentResponse::from);
    }

    public Mono<Void> process(PaymentRequestEvent event) {
        String idempotentKey = "payment:idempotent:" + event.orderId();

        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", Duration.ofHours(24))
                .flatMap(isNew -> {
                    if (!isNew) {
                        log.warn("중복 결제 요청 skip: orderId={}", event.orderId());
                        return Mono.empty();
                    }
                    return processPayment(event)
                            .onErrorResume(e -> reactiveRedisTemplate
                                    .delete(idempotentKey)
                                    .then(Mono.error(e)));
                });
    }

    private Mono<Void> processPayment(PaymentRequestEvent event) {
        return transactionalOperator.transactional(
                paymentRepository.save(Payment.create(event.orderId(), event.memberId(), event.amount()))
                        .flatMap(saved -> {
                            boolean success = event.amount() > 0;
                            PaymentStatus status = success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;
                            String message = success ? "결제 성공" : "결제 실패: 유효하지 않은 금액";

                            String payload;
                            try {
                                payload = objectMapper.writeValueAsString(
                                        new PaymentResultEvent(event.orderId(), message, status.name())
                                );
                            } catch (JsonProcessingException e) {
                                return Mono.error(e);
                            }

                            return paymentRepository.save(saved.withStatus(status))
                                    .flatMap(updated -> outboxRepository.save(
                                            Outbox.create("payment.result", payload)
                                    ));
                        })
        ).then();
    }
}
