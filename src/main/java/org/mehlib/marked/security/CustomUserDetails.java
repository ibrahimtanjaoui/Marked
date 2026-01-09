package org.mehlib.marked.security;

import org.mehlib.marked.dao.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation that wraps our User entities.
 * This bridges Spring Security with our domain model.
 */
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String familyName;
    private final String fullName;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, UserRole role) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.firstName = user.getFirstName();
        this.familyName = user.getFamilyName();
        this.fullName = user.getFullName() != null ? user.getFullName()
                : user.getFirstName() + " " + user.getFamilyName();
        this.role = role;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the user's database ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the user's first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the user's family name.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Returns the user's full name.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the user's role.
     */
    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
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

    @Override
    public boolean isEnabled() {
        return true;
    }
}
