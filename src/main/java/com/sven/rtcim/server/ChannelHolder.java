package com.sven.rtcim.server;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此类存储channel和用户的信息
 *
 * @author lanfangyi@haodf.com
 * @version 1.0
 * @since 2019/8/21 8:36 PM
 */
public class ChannelHolder {

    /**
     * key是userId。
     */
    private static ConcurrentHashMap<String, String> userChannelIdMap = new ConcurrentHashMap<>(500);

    private static ConcurrentHashMap<String, Channel> channelIdMap = new ConcurrentHashMap<>(500);


    public static void putUserKeyChannel(String userKey, String channelId) {
        userChannelIdMap.put(userKey, channelId);
        System.out.println("IM online counts=" + userChannelIdMap.size());

    }

    /**
     * 根据key拿到对应的channel
     *
     * @param userKey 用户的ID
     */
    public static Channel getChannel(String userKey) {
        String channelId = userChannelIdMap.get(userKey);
        if (StringUtils.isEmpty(channelId)) {
            return null;
        }
        return channelIdMap.get(channelId);
    }

    public static boolean putChannelNoUserId(String channelId, Channel channel) {
        return channelIdMap.put(channelId, channel) != null;
    }

    public static boolean removeChannelNoUserId(String channelId) {
        if (StringUtils.isEmpty(channelId)) {
            return false;
        }
        channelIdMap.remove(channelId);
        List<String> userKeyList = new ArrayList<>();
        for (Map.Entry<String, String> entry : userChannelIdMap.entrySet()) {
            if (channelId.equals(entry.getValue())) {
                userKeyList.add(entry.getKey());
            }
        }
        for (String useKey : userKeyList) {
            userChannelIdMap.remove(useKey);
            System.out.println("removeChannelNoUserId useKey=" + useKey);
        }
        return true;
    }

    public static int getImTotalCount() {
        return userChannelIdMap.size();
    }
}
