package com.commerce.event;

import com.commerce.domain.Order;
import com.commerce.repository.OrderRepository;
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

    @Transactional
    @KafkaListener(topics = "payment.result", groupId = "commerce-group")
    public void consume(PaymentResultEvent event) {
        log.info("결제 결과 수신: {}", event);
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문: " + event.orderId()));
        order.updateStatus(event.status());
    }
}
