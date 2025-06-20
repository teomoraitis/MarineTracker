package com.di.marinetracker.backendspringboot.services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.di.marinetracker.backendspringboot.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

// Implementation of UserDetails for Spring Security
// Needed to represent the authenticated user in the security context
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    // Unique identifier for the user
    private String id;

    // Username of the user
    private String username;

    // Email address of the user
    private String email;

    // Password (ignored in JSON serialization)
    @JsonIgnore
    private String password;

    // Authorities (roles/permissions) granted to the user
    private Collection<? extends GrantedAuthority> authorities;

    // Constructor to initialize all fields
    public UserDetailsImpl(String id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // Builds a UserDetailsImpl from a User entity
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    // Returns the authorities granted to the user
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // Returns the user's unique identifier
    public String getId() {
        return id;
    }

    // Returns the user's email address
    public String getEmail() {
        return email;
    }

    // Returns the user's password
    @Override
    public String getPassword() {
        return password;
    }

    // Returns the user's username
    @Override
    public String getUsername() {
        return username;
    }

    // Indicates whether the user's account has expired
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Indicates whether the user is locked or unlocked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Indicates whether the user's credentials (password) have expired
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Indicates whether the user is enabled or disabled
    @Override
    public boolean isEnabled() {
        return true;
    }

    // Checks equality based on user id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
