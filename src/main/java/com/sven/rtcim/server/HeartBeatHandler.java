package com.sven.rtcim.server;

import com.sven.rtcim.redis.RedisCache;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳支持
 *
 * @author lanfangyi@haodf.com
 * @version 1.0
 * @since 2019/9/20 3:35 PM
 */
@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            // 强制类型转换
            IdleStateEvent event = (IdleStateEvent) evt;

            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                channel.close();
                log.info("进入空闲关闭channel");
            } else if (event.state() == IdleState.WRITER_IDLE) {

            } else if (event.state() == IdleState.ALL_IDLE) {
            }
        }
    }
}
