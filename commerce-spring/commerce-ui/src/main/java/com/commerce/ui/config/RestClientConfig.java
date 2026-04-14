package com.commerce.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("commerceRestClient")
    public RestClient commerceRestClient(
            @Value("${commerce.api.url}") String baseUrl,
            @Value("${commerce.api.prefix}") String prefix) {
        return RestClient.builder()
                .baseUrl(baseUrl + prefix)
                .build();
    }

    @Bean("paymentRestClient")
    public RestClient paymentRestClient(
            @Value("${payment.api.url}") String baseUrl,
            @Value("${payment.api.prefix}") String prefix) {
        return RestClient.builder()
                .baseUrl(baseUrl + prefix)
                .build();
    }

}
