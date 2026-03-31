package com.commerce.ui.client;

import com.commerce.ui.dto.DailySettlementResponse;
import com.commerce.ui.dto.SettlementFilterRequest;
import com.commerce.ui.dto.SettlementRecordResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SettlementClient {

    private final RestClient restClient;

    public SettlementClient(@Qualifier("settlementRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SettlementRecordResponse> getRecords(SettlementFilterRequest filter) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/settlements")
                        .queryParamIfPresent("sellerId", Optional.ofNullable(filter.getSellerId()))
                        .queryParamIfPresent("status", Optional.ofNullable(filter.getStatus()).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("startDate", Optional.ofNullable(filter.getStartDate()).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("endDate", Optional.ofNullable(filter.getEndDate()).filter(s -> !s.isBlank()))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<DailySettlementResponse> getDailySettlements(Long sellerId, String settlementDate) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/settlements/daily")
                        .queryParamIfPresent("sellerId", Optional.ofNullable(sellerId))
                        .queryParamIfPresent("settlementDate", Optional.ofNullable(settlementDate).filter(s -> !s.isBlank()))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, String> triggerJob(String settlementDate) {
        return restClient.post()
                .uri("/settlements/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("settlementDate", settlementDate))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
