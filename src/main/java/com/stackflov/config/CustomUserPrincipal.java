package com.stackflov.config;

import com.stackflov.domain.Role;
import com.stackflov.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String nickname;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomUserPrincipal(Long id, String email, String nickname, Role role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.authorities = authorities;
    }

    public static CustomUserPrincipal from(User user) {
        String roleName = user.getRole() != null ? user.getRole().name() : Role.USER.name();
        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
        );
    }

    // === UserDetails (패스워드/계정상태는 JWT 인증이라 의미 없음. 전부 true) ===
    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
