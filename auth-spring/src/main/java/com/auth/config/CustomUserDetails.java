package com.auth.config;

import com.auth.user.domain.Role;
import com.auth.user.domain.User;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private String email;
    private String password;
    private List<SimpleGrantedAuthority> roles;

    @Builder
    private CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roles = user.getRoles().stream().map(Role::getName)
                .reduce((a, b) -> a + "," + b)
                .map(role -> List.of(new SimpleGrantedAuthority(role)))
                .orElse(List.of());
    }

    @Override
    public @Nonnull Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
