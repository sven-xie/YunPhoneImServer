package com.sven.rtcim.redis;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserMemberInfo implements Serializable {

    private long userId;


    /**
     * 飞鸽用户的可用时长
     */
    private long totalCanUseTime;

}
