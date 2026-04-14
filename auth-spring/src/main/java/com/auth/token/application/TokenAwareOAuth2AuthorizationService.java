package com.auth.token.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

/**
 * JdbcOAuth2AuthorizationService를 감싸 Refresh Token을 Redis에도 동기화하는 데코레이터.
 * - save()  : JDBC 저장 후 Refresh Token이 있으면 Redis에도 저장
 * - remove(): JDBC 삭제 후 Redis에서도 Refresh Token 삭제
 * - find*() : JDBC에 위임 (토큰 검증은 Spring Authorization Server가 처리)
 */
@RequiredArgsConstructor
public class TokenAwareOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final OAuth2AuthorizationService delegate;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Override
    public void save(OAuth2Authorization authorization) {
        delegate.save(authorization);
        syncRefreshTokenToRedis(authorization);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        delegate.remove(authorization);
        evictRefreshTokenFromRedis(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return delegate.findByToken(token, tokenType);
    }

    private void syncRefreshTokenToRedis(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2RefreshToken> token = authorization.getToken(OAuth2RefreshToken.class);
        if (token == null) {
            return;
        }
        refreshTokenRedisRepository.save(
                token.getToken().getTokenValue(),
                authorization.getPrincipalName(),
                authorization.getRegisteredClientId(),
                authorization.getAuthorizedScopes()
        );
    }

    private void evictRefreshTokenFromRedis(OAuth2Authorization authorization) {
        OAuth2Authorization.Token<OAuth2RefreshToken> token = authorization.getToken(OAuth2RefreshToken.class);
        if (token == null) {
            return;
        }
        refreshTokenRedisRepository.delete(token.getToken().getTokenValue());
    }
}
