package com.auth.user.application;

import com.auth.user.domain.Role;
import com.auth.user.domain.RoleRepository;
import com.auth.user.domain.SocialAccount;
import com.auth.user.domain.SocialAccountRepository;
import com.auth.user.domain.SocialProvider;
import com.auth.user.domain.User;
import com.auth.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialUserRegistrationService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RoleRepository roleRepository;

    /**
     * 소셜 로그인 시 기존 계정을 찾거나 새 계정을 생성·연동한다.
     * - provider + providerId 로 기존 SocialAccount 조회 → 있으면 연결된 User 반환
     * - 없으면 이메일로 기존 User 조회 → 있으면 SocialAccount 연동 후 반환
     * - 둘 다 없으면 User + SocialAccount 신규 생성
     */
    public User findOrRegister(SocialProvider provider, String providerId, String email, String nickname) {
        return socialAccountRepository.findByProviderAndProviderId(provider, providerId)
                .map(SocialAccount::getUser)
                .orElseGet(() -> createOrLinkUser(email, nickname, provider, providerId));
    }

    private User createOrLinkUser(String email, String nickname, SocialProvider provider, String providerId) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    Role userRole = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new IllegalStateException("ROLE_USER not found in database"));
                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .build();
                    newUser.verifyEmail();
                    newUser.addRole(userRole);
                    return userRepository.save(newUser);
                });

        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .build();
        socialAccountRepository.save(socialAccount);
        return user;
    }
}
