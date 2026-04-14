package com.auth.user.application;

import com.auth.user.domain.SocialProvider;
import com.auth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialUserRegistrationService registrationService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

        String providerId = extractProviderId(provider, oauth2User);
        String email = extractEmail(provider, oauth2User);
        String nickname = extractNickname(provider, oauth2User);

        User user = registrationService.findOrRegister(provider, providerId, email, nickname);
        return new CustomOAuth2User(oauth2User, user.getEmail(), user.getRoles());
    }

    private String extractProviderId(SocialProvider provider, OAuth2User oauth2User) {
        return switch (provider) {
            case GOOGLE -> oauth2User.getAttribute("sub");
            case KAKAO -> String.valueOf(oauth2User.<Number>getAttribute("id"));
        };
    }

    private String extractEmail(SocialProvider provider, OAuth2User oauth2User) {
        return switch (provider) {
            case GOOGLE -> oauth2User.getAttribute("email");
            case KAKAO -> {
                Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
                    throw new OAuth2AuthenticationException(
                            new OAuth2Error("missing_email"), "카카오 계정 이메일 제공에 동의가 필요합니다.");
                }
                yield (String) kakaoAccount.get("email");
            }
        };
    }

    @SuppressWarnings("unchecked")
    private String extractNickname(SocialProvider provider, OAuth2User oauth2User) {
        return switch (provider) {
            case GOOGLE -> oauth2User.getAttribute("name");
            case KAKAO -> {
                Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                if (kakaoAccount != null) {
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    if (profile != null && profile.containsKey("nickname")) {
                        yield (String) profile.get("nickname");
                    }
                }
                yield "카카오 사용자";
            }
        };
    }
}
