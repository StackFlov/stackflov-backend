package com.stackflov.config;

import com.stackflov.domain.Role;
import com.stackflov.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final Role role;
    private final List<GrantedAuthority> authorities;

    private CustomUserPrincipal(Long id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.authorities = role == null
             ? List.of()
             : List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
     public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(user.getId(), user.getEmail(), user.getRole());
     }
     public Long getId() { return id; }
    public String getEmail() { return email; }
    public Role getRoleEnum() { return role; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}