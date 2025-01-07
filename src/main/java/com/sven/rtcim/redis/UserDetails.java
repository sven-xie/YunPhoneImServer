package com.sven.rtcim.redis;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author sven
 * @date 2025/01/07
 **/
public interface UserDetails extends Serializable {

    Collection<? extends GrantedAuthority> getAuthorities();

    String getPassword();

    String getUsername();

    boolean isAccountNonExpired();

    boolean isAccountNonLocked();

    boolean isCredentialsNonExpired();

    boolean isEnabled();
}