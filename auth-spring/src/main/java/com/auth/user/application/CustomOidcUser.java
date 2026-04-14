package com.auth.user.application;

import com.auth.user.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomOidcUser extends DefaultOidcUser {

    private final String email;
    private final Set<GrantedAuthority> roleAuthorities;

    public CustomOidcUser(OidcIdToken idToken, OidcUserInfo userInfo, String email, Set<Role> roles) {
        super(Set.of(), idToken, userInfo, "sub");
        this.email = email;
        this.roleAuthorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleAuthorities;
    }

    @Override
    public String getName() {
        return email;
    }
}
