package com.settlement.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.settlement.service.SettlementRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final SettlementRecordService settlementRecordService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.result", groupId = "settlement-group")
    public void consume(String message) {
        PaymentResultEvent event;

        try {
            event = objectMapper.readValue(message, PaymentResultEvent.class);
        } catch (Exception e) {
            log.error("payment.result 메시지 파싱 실패: {}", message, e);
            return;
        }

        process(event);
    }

    private void process(PaymentResultEvent event) {
        if ("COMPLETED".equals(event.status())) {
            settlementRecordService.createRecord(event);
        } else if ("REFUNDED".equals(event.status())) {
            settlementRecordService.cancelRecord(event);
        } else {
            log.debug("정산 처리 불필요한 상태 — orderId={}, status={}", event.orderId(), event.status());
        }
    }
}
