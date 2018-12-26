package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.bean.PackageData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author bulingbuu
 * @date 18-12-26 下午4:12
 */
@Slf4j
public class EchoHandler extends SimpleChannelInboundHandler<PackageData> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PackageData msg) throws Exception {
        log.info("Test:" + msg.toString());
        ctx.writeAndFlush(msg);
    }
}
