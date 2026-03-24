package com.commerce.ui.client;

import com.commerce.ui.dto.PaymentFilterRequest;
import com.commerce.ui.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(@Qualifier("paymentRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void confirmPayment(String paymentKey, Long orderId, Long memberId, String tossOrderId, int amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("memberId", memberId);
        body.put("tossOrderId", tossOrderId);
        body.put("amount", amount);
        restClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public List<PaymentResponse> getPayments(PaymentFilterRequest filter) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments")
                        .queryParamIfPresent("memberId", Optional.ofNullable(filter.getMemberId()))
                        .queryParamIfPresent("orderId", Optional.ofNullable(filter.getOrderId()))
                        .queryParamIfPresent("status", Optional.ofNullable(filter.getStatus()).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("startDate", Optional.ofNullable(filter.getStartDate()).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("endDate", Optional.ofNullable(filter.getEndDate()).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("minAmount", Optional.ofNullable(filter.getMinAmount()))
                        .queryParamIfPresent("maxAmount", Optional.ofNullable(filter.getMaxAmount()))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void refund(Long orderId, String reason) {
        restClient.post()
                .uri("/payments/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "orderId", orderId,
                        "reason", reason
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
