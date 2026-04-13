package com.auth.user.application;

import com.auth.user.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final String email;
    private final Set<GrantedAuthority> authorities;

    public CustomOAuth2User(OAuth2User delegate, String email, Set<Role> roles) {
        this.delegate = delegate;
        this.email = email;
        this.authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return email;
    }
}
