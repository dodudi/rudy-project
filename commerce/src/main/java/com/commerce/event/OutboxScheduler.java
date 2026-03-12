package com.commerce.event;

import com.commerce.domain.Outbox;
import com.commerce.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {
        List<Outbox> pendingList = outboxRepository.findByStatus(Outbox.OutboxStatus.PENDING);
        for (Outbox outbox : pendingList) {
            try {
                PaymentRequestEvent event = objectMapper.readValue(outbox.getPayload(), PaymentRequestEvent.class);
                kafkaTemplate.send(outbox.getTopic(), event).get(); // 동기 전송 (성공 확인 후 상태 변경)
                outbox.markPublished();
                log.info("Outbox 발행 성공: outboxId={}", outbox.getId());
            } catch (Exception e) {
                log.error("Outbox 발행 실패, 다음 스케줄에 재시도: outboxId={}", outbox.getId(), e);
            }
        }
    }
}
