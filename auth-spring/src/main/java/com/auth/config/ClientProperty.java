package com.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ClientProperty {

    private final String id;
    private final String secret;
    private final String redirectUri;
    private final String postLogoutRedirectUri;

    public ClientProperty(
            @Value("${auth.client.id}") String id,
            @Value("${auth.client.secret}") String secret,
            @Value("${auth.client.redirect-uri}") String redirectUri,
            @Value("${auth.client.post-logout-redirect-uri}") String postLogoutRedirectUri
    ) {
        this.id = id;
        this.secret = secret;
        this.redirectUri = redirectUri;
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }
}
