package com.auth.user.application;

import com.auth.user.domain.SocialProvider;
import com.auth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final SocialUserRegistrationService registrationService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String nickname = oidcUser.getFullName() != null ? oidcUser.getFullName() : email;

        User user = registrationService.findOrRegister(SocialProvider.GOOGLE, providerId, email, nickname);
        return new CustomOidcUser(oidcUser.getIdToken(), oidcUser.getUserInfo(), user.getEmail(), user.getRoles());
    }
}
