package com.etransportation.security.service;

import com.etransportation.enums.AccountStatus;
import com.etransportation.model.Account;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsImpl implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Boolean isEnabled;
    private Boolean isAccountNonLocked;

    public UserDetailsImpl(Account user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = user.getRoles().stream().map(role -> role.getName().name()).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        this.isAccountNonLocked = user.getStatus() == AccountStatus.BLOCKED ? false : true;
        this.isEnabled = user.getStatus() == AccountStatus.RETIRED ? false : true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
