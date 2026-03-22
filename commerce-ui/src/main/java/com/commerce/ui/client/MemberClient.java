package com.commerce.ui.client;

import com.commerce.ui.dto.MemberCreateRequest;
import com.commerce.ui.dto.MemberResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MemberClient {

    private final RestClient restClient;

    public MemberClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public MemberResponse createMember(MemberCreateRequest request) {
        return restClient.post()
                .uri("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MemberResponse.class);
    }

    public List<MemberResponse> getMembers() {
        return restClient.get()
                .uri("/members")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
