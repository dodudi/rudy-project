package com.commerce.event;

import com.commerce.domain.Order;
import com.commerce.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "payment.result", groupId = "commerce-group")
    public void consume(String message) {

        PaymentResultEvent event;

        try {
            event = objectMapper.readValue(message, PaymentResultEvent.class);
        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", message, e);
            return;
        }

        process(event);
    }

    private void process(PaymentResultEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow();
        order.updateStatus(event.status());
    }
}
