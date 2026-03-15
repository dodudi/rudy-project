package com.payment.event;

import com.payment.dto.PaymentRequestEvent;
import com.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "payment.request", groupId = "payment-group")
    public void consume(PaymentRequestEvent event) {
        log.info("결제 요청 수신: orderId={}, amount={}", event.orderId(), event.amount());
        paymentService.process(event)
                .subscribeOn(Schedulers.boundedElastic())
                .block();
    }
}
