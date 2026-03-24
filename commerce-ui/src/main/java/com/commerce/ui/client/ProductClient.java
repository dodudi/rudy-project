package com.commerce.ui.client;

import com.commerce.ui.dto.ProductCreateRequest;
import com.commerce.ui.dto.ProductCreateResponse;
import com.commerce.ui.dto.ProductFilterRequest;
import com.commerce.ui.dto.ProductUpdateRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Qualifier("commerceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductCreateResponse getProduct(Long id) {
        return restClient.get()
                .uri("/products/" + id)
                .retrieve()
                .body(ProductCreateResponse.class);
    }

    public ProductCreateResponse updateProduct(Long id, ProductUpdateRequest request) {
        return restClient.put()
                .uri("/products/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ProductCreateResponse.class);
    }

    public ProductCreateResponse createProduct(ProductCreateRequest request) {
        return restClient.post()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ProductCreateResponse.class);
    }

    public List<ProductCreateResponse> getProducts(ProductFilterRequest filter) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products")
                        .queryParamIfPresent("name", java.util.Optional.ofNullable(filter.getName()))
                        .queryParam("minPrice", filter.getMinPrice() != null ? filter.getMinPrice() : 0)
                        .queryParam("maxPrice", filter.getMaxPrice() != null ? filter.getMaxPrice() : 0)
                        .queryParam("hasStock", filter.isHasStock())
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
