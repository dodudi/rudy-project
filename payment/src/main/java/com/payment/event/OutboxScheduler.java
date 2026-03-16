package com.payment.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.domain.Outbox;
import com.payment.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final PaymentResultProducer paymentResultProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void relay() {
        outboxRepository.findByStatus(Outbox.OutboxStatus.PENDING.name())
                .flatMap(this::publish)
                .subscribeOn(Schedulers.boundedElastic())
                .blockLast();
    }

    private Mono<Outbox> publish(Outbox outbox) {
        PaymentResultEvent event;
        try {
            event = objectMapper.readValue(outbox.getPayload(), PaymentResultEvent.class);
        } catch (Exception e) {
            log.error("Outbox payload 역직렬화 실패: id={}", outbox.getId(), e);
            return outboxRepository.save(outbox.markFailed());
        }

        return paymentResultProducer.send(event)
                .then(outboxRepository.save(outbox.markPublished()))
                .doOnSuccess(o -> log.info("Outbox 발행 성공: id={}", outbox.getId()))
                .onErrorResume(e -> {
                    log.error("Outbox 발행 실패: id={}", outbox.getId(), e);
                    return outboxRepository.save(outbox.markFailed());
                });
    }
}
