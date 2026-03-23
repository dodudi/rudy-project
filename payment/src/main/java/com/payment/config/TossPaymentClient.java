package com.payment.config;

import com.payment.dto.PaymentRequest;
import com.payment.dto.TossConfirmResponse;
import com.payment.exception.TossPaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final WebClient tossWebClient;

    public Mono<TossConfirmResponse> confirm(PaymentRequest request) {
        log.info("Toss confirm 요청: paymentKey={}, orderId={}, amount={}", request.paymentKey(), request.tossOrderId(), request.amount());

        return tossWebClient.post()
                .uri("/v1/payments/confirm")
                .bodyValue(Map.of(
                        "paymentKey", request.paymentKey(),
                        "orderId", request.tossOrderId(),
                        "amount", request.amount()
                ))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .doOnNext(body -> log.error("Toss API 오류: status={}, body={}", response.statusCode(), body))
                                .flatMap(body -> Mono.error(new TossPaymentException("Toss confirm 실패: " + body)))
                )
                .bodyToMono(TossConfirmResponse.class);
    }

    public Mono<Void> cancel(String paymentKey, String cancelReason) {
        log.info("Toss cancel 요청: paymentKey={}", paymentKey);

        return tossWebClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .bodyValue(Map.of("cancelReason", cancelReason != null ? cancelReason : "고객 요청"))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .doOnNext(body -> log.error("Toss cancel 오류: status={}, body={}", response.statusCode(), body))
                                .flatMap(body -> Mono.error(new TossPaymentException("Toss cancel 실패: " + body)))
                )
                .bodyToMono(Void.class);
    }
}
