package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.bean.PackageData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author bulingbuu
 * @date 18-12-18 下午5:30
 * <p>
 * 消息调度处理
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageDispatchHandler extends SimpleChannelInboundHandler<PackageData> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("channelActive");
        PackageData data = new PackageData();
        data.setEncryp((short) 1);
        data.setMsgId((short) 2);
        data.setTarget("QWERTYU12345");
        data.setSource(3);
        byte[] bytes = new byte[10];
        bytes[0]=0x7e;
        for (int i = 1; i < bytes.length-1; i++) {
            bytes[i] = (byte) (i + 10);
        }
        bytes[9]=0x7d;
        data.setBody(bytes);
        ctx.writeAndFlush(data);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PackageData msg) throws Exception {
        log.info(msg.toString());
    }
}
