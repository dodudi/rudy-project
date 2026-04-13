package com.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * Local 프로파일 전용 개발용 OAuth2 클라이언트 초기 등록.
 * 운영 환경에서는 Admin API를 통해 클라이언트를 등록한다.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    private final RegisteredClientRepository registeredClientRepository;

    @Override
    public void run(ApplicationArguments args) {
        registerSpaClient();
        registerM2mClient();
    }

    private void registerSpaClient() {
        if (registeredClientRepository.findByClientId("dev-spa-client") != null) {
            return;
        }
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("dev-spa-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)   // 공개 클라이언트 (PKCE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:3000/callback")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("read")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)               // PKCE 필수
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)           // Refresh Token 회전
                        .build())
                .build();
        registeredClientRepository.save(client);
        log.info("[DEV] Registered OAuth2 client: dev-spa-client");
    }

    private void registerM2mClient() {
        if (registeredClientRepository.findByClientId("dev-m2m-client") != null) {
            return;
        }
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("dev-m2m-client")
                .clientSecret("{noop}dev-m2m-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .scope("write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(15))
                        .build())
                .build();
        registeredClientRepository.save(client);
        log.info("[DEV] Registered OAuth2 client: dev-m2m-client");
    }
}
