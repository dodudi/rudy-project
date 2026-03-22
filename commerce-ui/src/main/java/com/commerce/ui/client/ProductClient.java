package com.commerce.ui.client;

import com.commerce.ui.dto.ProductCreateRequest;
import com.commerce.ui.dto.ProductCreateResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductCreateResponse createProduct(ProductCreateRequest request) {
        return restClient.post()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ProductCreateResponse.class);
    }

    public List<ProductCreateResponse> getProducts() {
        return restClient.get()
                .uri("/products")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
