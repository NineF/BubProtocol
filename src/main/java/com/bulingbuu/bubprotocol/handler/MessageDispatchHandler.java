package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.bean.PackageData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author bulingbuu
 * @date 18-12-18 下午5:30
 * <p>
 * 消息调度处理
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageDispatchHandler extends SimpleChannelInboundHandler<PackageData> {
    private AtomicInteger num = new AtomicInteger(1);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        PackageData data = new PackageData();
        int x=num.getAndAdd(1);
        data.setEncryp((short) 1);
        data.setMsgId((short)x);
        data.setTarget("QWERTYU12345");
        data.setSource(x);
        byte[] bytes = new byte[10];
        bytes[0]=0x7e;
        for (int i = 1; i < bytes.length-1; i++) {
            bytes[i] = (byte) (i + 10);
        }
        bytes[9]=0x7d;
        data.setBody(bytes);
        ctx.writeAndFlush(data);


//        PackageData data1 = new PackageData();
//        data1.setEncryp((short) 2);
//        data1.setMsgId((short) 3);
//        data1.setTarget("QWERTYU12346");
//        data1.setSource(4);
//        byte[] bytes1 = new byte[10];
//        bytes1[0]=0x7d;
//        for (int i = 1; i < bytes1.length-1; i++) {
//            bytes1[i] = (byte) (i + 20);
//        }
//        bytes1[9]=0x7e;
//        data1.setBody(bytes1);
//        ctx.writeAndFlush(data1);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PackageData msg) throws Exception {
        log.info(msg.toString());
        Thread.sleep(1000);
        ctx.writeAndFlush(msg);
    }
}
