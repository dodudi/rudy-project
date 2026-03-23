package com.commerce.ui.client;

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

    public List<OrderResponse> getOrders() {
        return restClient.get()
                .uri("/orders")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
