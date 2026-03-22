package com.commerce.ui.client;

import com.commerce.ui.dto.OrderRequest;
import com.commerce.ui.dto.OrderResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        return restClient.post()
                .uri("/api/commerce/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OrderResponse.class);
    }
}
