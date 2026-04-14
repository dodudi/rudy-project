package com.commerce.ui.client;

import com.commerce.ui.dto.OrderFilterRequest;
import com.commerce.ui.dto.OrderRequest;
import com.commerce.ui.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Qualifier("commerceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        return restClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OrderResponse.class);
    }

    public void cancelOrder(Long orderId) {
        restClient.post()
                .uri("/orders/{orderId}/cancel", orderId)
                .retrieve()
                .toBodilessEntity();
    }

    public List<OrderResponse> getOrders(OrderFilterRequest filter) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orders")
                        .queryParamIfPresent("memberId", java.util.Optional.ofNullable(filter.getMemberId()))
                        .queryParamIfPresent("status", java.util.Optional.ofNullable(filter.getStatus()).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("startDate", java.util.Optional.ofNullable(filter.getStartDate()).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("endDate", java.util.Optional.ofNullable(filter.getEndDate()).filter(s -> !s.isBlank()))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
