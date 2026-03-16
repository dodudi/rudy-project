package com.payment.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.request", groupId = "payment-group")
    public void consume(String message) {

        PaymentRequestEvent event;

        try {
            event = objectMapper.readValue(message, PaymentRequestEvent.class);
        } catch (Exception e) {
            log.error("Kafka 메시지 역직렬화 실패: {}", message, e);
            kafkaTemplate.send("payment.request.DLT", message);
            return;
        }

        log.info("결제 요청 수신: orderId={}, amount={}", event.orderId(), event.amount());

        try {
            paymentService.process(event)
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();
        } catch (Exception e) {
            log.error("결제 처리 실패: event={}", event, e);
            throw e;
        }
    }
}
