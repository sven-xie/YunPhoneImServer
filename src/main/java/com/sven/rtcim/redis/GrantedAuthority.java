package com.sven.rtcim.redis;

import java.io.Serializable;

/**
 * @author sven
 * @date 2025/01/07
 **/
public interface GrantedAuthority extends Serializable {
    String getAuthority();
}
