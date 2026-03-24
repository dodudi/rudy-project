package com.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.config.TossPaymentClient;
import com.payment.domain.Outbox;
import com.payment.domain.Payment;
import com.payment.dto.PaymentFilterRequest;
import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.RefundRequest;
import com.payment.event.PaymentResultEvent;
import com.payment.exception.ErrorCode;
import com.payment.exception.PaymentException;
import com.payment.exception.TossPaymentException;
import com.payment.repository.OutboxRepository;
import com.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.payment.repository.PaymentCriteria;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private static final String LOCK_PREFIX = "payment:lock:";
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);

    public Mono<PaymentResponse> payment(PaymentRequest request) {
        return acquireLock(request.orderId())
                .then(Mono.defer(() -> tossPaymentClient.confirm(request)))
                .flatMap(tossResponse -> savePaymentWithEvent(request, PaymentResultEvent.completed(request.orderId())))
                .map(PaymentResponse::from)
                .onErrorResume(TossPaymentException.class, e ->
                        savePaymentWithEvent(request, PaymentResultEvent.failed(request.orderId(), e.getMessage()))
                                .flatMap(saved -> releaseLock(request.orderId()).thenReturn(saved))
                                .map(PaymentResponse::from)
                )
                .onErrorResume(PaymentException.class, Mono::error)
                .doOnSuccess(r -> log.info("결제 처리 완료"))
                .doOnError(e -> log.error("결제 처리 실패"));
    }

    private Mono<Void> acquireLock(Long orderId) {
        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX + orderId, "1", LOCK_TTL)
                .flatMap(acquired -> acquired ? Mono.empty() : Mono.error(new PaymentException(ErrorCode.DUPLICATE_PAYMENT_CONFIRMATION)));
    }

    private Mono<Void> releaseLock(Long orderId) {
        return reactiveRedisTemplate.delete(LOCK_PREFIX + orderId).then();
    }

    private Mono<Payment> savePaymentWithEvent(PaymentRequest request, PaymentResultEvent event) {
        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .memberId(request.memberId())
                .paymentKey(request.paymentKey())
                .paymentId(request.tossOrderId())
                .amount(request.amount())
                .build();

        try {
            String payload = objectMapper.writeValueAsString(event);
            Payment marked = event.status().equals("COMPLETED") ? payment.markCompleted() : payment.markFailed();
            return paymentRepository.save(marked)
                    .flatMap(saved -> outboxRepository.save(Outbox.builder().topic("payment.result").payload(payload).build())
                            .thenReturn(saved));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    public Mono<PaymentResponse> refund(RefundRequest request) {
        return paymentRepository.findByOrderId(request.orderId())
                .switchIfEmpty(Mono.error(new PaymentException(ErrorCode.PAYMENT_NOT_FOUND)))
                .filter(Payment::isRefundable)
                .switchIfEmpty(Mono.error(new PaymentException(ErrorCode.NOT_REFUNDABLE_PAYMENT)))
                .flatMap(payment -> tossPaymentClient.cancel(payment.getPaymentKey(), request.reason())
                        .thenReturn(payment.markRefunded()))
                .flatMap(this::saveRefundWithEvent)
                .map(PaymentResponse::from)
                .doOnSuccess(r -> log.info("환불 처리 완료"))
                .doOnError(e -> log.error("환불 처리 실패"));
    }

    private Mono<Payment> saveRefundWithEvent(Payment payment) {
        try {
            String payload = objectMapper.writeValueAsString(PaymentResultEvent.refunded(payment.getOrderId()));
            return paymentRepository.save(payment)
                    .flatMap(saved -> outboxRepository.save(Outbox.builder().topic("payment.result").payload(payload).build())
                            .thenReturn(saved));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    public Flux<PaymentResponse> getPayments(PaymentFilterRequest filter) {
        return r2dbcEntityTemplate.select(PaymentCriteria.withFilters(filter), Payment.class)
                .map(PaymentResponse::from);
    }
}
