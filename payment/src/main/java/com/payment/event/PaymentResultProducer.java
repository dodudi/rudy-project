package com.payment.event;

import com.payment.dto.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Void> send(PaymentResultEvent event) {
        return Mono.fromFuture(kafkaTemplate.send("payment.result", event).toCompletableFuture())
                .doOnSuccess(r -> log.info("결제 결과 발행 성공: orderId={}, status={}", event.orderId(), event.status()))
                .doOnError(e -> log.error("결제 결과 발행 실패: orderId={}", event.orderId(), e))
                .then()
                .subscribeOn(Schedulers.boundedElastic());
    }
}
