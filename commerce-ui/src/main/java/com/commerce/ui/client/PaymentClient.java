package com.commerce.ui.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(@Qualifier("paymentRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void confirmPayment(String paymentKey, Long orderId, String tossOrderId, int amount) {
        restClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "paymentKey", paymentKey,
                        "orderId", orderId,
                        "tossOrderId", tossOrderId,
                        "amount", amount
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
