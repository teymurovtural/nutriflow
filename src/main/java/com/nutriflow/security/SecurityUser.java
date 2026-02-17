package com.nutriflow.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class SecurityUser implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final boolean isActive;

    // Constructor
    public SecurityUser(Long id, String email, String password, String role, boolean isActive) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.role);
    }

    public boolean isAdmin() {
        // Həm Super Admin, həm də Sub-Admin "admin" sayılır
        return "ADMIN".equals(this.role) || "SUPER_ADMIN".equals(this.role);
    }

    public boolean isDietitian() {
        return "DIETITIAN".equals(this.role);
    }

    public boolean isCaterer() {
        return "CATERER".equals(this.role);
    }

    public boolean isUser() {
        return "USER".equals(this.role);
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security-də rollar "ROLE_" prefix-i ilə tanınır
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}