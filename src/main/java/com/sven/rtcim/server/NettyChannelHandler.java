package com.sven.rtcim.server;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.sven.rtcim.redis.LoginUser;
import com.sven.rtcim.redis.RedisCache;
import com.sven.rtcim.utils.DateUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author lanfangyi@haodf.com
 * @version 1.0
 * @since 2019/7/30 2:22 PM
 */
@Slf4j
public class NettyChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final static String secret = "xie1203";

    private RedisCache redisCache;

    public NettyChannelHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext context) throws Exception {
        NettyConfig.group.add(context.channel());
        log.info("客户端建立连接，时间：{}", DateUtil.getNow());
        log.info("channelid:" + context.channel().id().asLongText());
        //将channel存入
        ChannelHolder.putChannelNoUserId(context.channel().id().toString(), context.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext context) throws Exception {
        NettyConfig.group.remove(context.channel());
        log.info("客户端与服务端连接断开，时间：{}", DateUtil.getNow());
        ChannelHolder.removeChannelNoUserId(context.channel().id().toString());
        log.info("清除channel信息成功");
    }

    /**
     * 出现异常的时候调用
     *
     * @param context
     * @param throwable
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) {
        log.info("捕捉到异常");
        throwable.printStackTrace();
        context.close();
    }

    /**
     * 服务端处理客户端websocke请求的核心方法
     *
     * @param channelHandlerContext
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame frame) {
//        log.info("服务端处理客户端websocke请求的核心方法");
        //处理客户端向服务端发起的http握手请求
        try {
            handWebSocketFrame(channelHandlerContext, frame);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 处理客户端与服务端之间的websocket业务
     *
     * @param context
     * @param webSocketFrame
     */
    private void handWebSocketFrame(ChannelHandlerContext context, WebSocketFrame webSocketFrame) {
        if (webSocketFrame instanceof TextWebSocketFrame) {//判断是否是文本消息
            //返回应答消息
            //获取客户端向服务端发送的消息
            String content = ((TextWebSocketFrame) webSocketFrame).text();
            if ("ping".equals(content)) {
                return;
            }
            log.info("服务端收到客户端的消息：" + content);
            JSONObject object = JSONObject.parseObject(content);
            String type = object.getString("type");
            switch (type) {
                case "join": {
                    String userId = object.getString("userId");
                    String token = object.getString("token");
                    if (StringUtils.isNotEmpty(token)
                            && StringUtils.isNotEmpty(userId)
                            && userId.length() > 16) {
                        Claims claims = Jwts.parser()
                                .setSigningKey(secret)
                                .parseClaimsJws(token)
                                .getBody();
                        // 解析对应的权限以及用户信息
                        String uuid = (String) claims.get("login_user_key");
                        String userKey = "login_tokens:" + uuid;
                        JSONObject userJson = redisCache.getCacheObject(userKey);
                        if (userJson != null) {
                            JSONObject sysUser = userJson.getJSONObject("user");
                            if (sysUser != null) {
                                String userName = userId.substring(0, userId.length() - 16);
                                if (userName.equals(sysUser.getString("userName"))) {
                                    ChannelHolder.putUserKeyChannel(userId, context.channel().id().toString());
                                    Map<String, Object> msg = ImmutableMap.of(
                                            "code", 200);
                                    context.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
                                    return;
                                }
                            }
                        }
                    }
                    Map<String, Object> msg = ImmutableMap.of(
                            "code", -100);
                    context.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
                    break;
                }
                case "leave": {//离开房间
                    String userId = object.getString("userId");
                    break;
                }
                case "offer": {//转发offer
                    String userId = object.getString("userId");
                    String peerId = object.getString("peerId");
                    String text = object.getString("text");
                    Channel peerChannel = ChannelHolder.getChannel(peerId);

                    if (peerChannel != null) {
                        Map<String, Object> msg = ImmutableMap.of(
                                "code", 0,
                                "peerId", userId,
                                "text", text);
                        peerChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
                    } else {
                        // 发送失败；对方不在线
                        Channel channel = ChannelHolder.getChannel(userId);
                        Map<String, Object> msg = ImmutableMap.of(
                                "code", -1);
                        channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
                    }
                    break;
                }
            }
        } else {
            log.info("handWebSocketFrame：........");
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //完成握手
            log.info("握手完成");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}

