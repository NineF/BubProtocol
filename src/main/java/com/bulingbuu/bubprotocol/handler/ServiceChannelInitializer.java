package com.bulingbuu.bubprotocol.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * @author bulingbuu
 * @date 18-10-9 上午11:18
 */
public class ServiceChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(
                new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[]{0x7e}),
                        Unpooled.copiedBuffer(new byte[]{0x7e, 0x7e})));

        pipeline.addLast(new MessageUnFixDecoder());
        pipeline.addLast(new MessageFixEncoder());

        pipeline.addLast(new MessageWrapperDecoder());
        pipeline.addLast(new PackageAggregator());
        pipeline.addLast(new MessageWrapperEncoder());
        pipeline.addLast(new MessageDispatchHandler());

    }
}
