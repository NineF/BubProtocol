package com.bulingbuu.bubprotocol;

import com.bulingbuu.bubprotocol.constant.NettyConstant;
import com.bulingbuu.bubprotocol.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * @author bulingbuu
 * @date 18-12-26 下午4:09
 */
public class Client {

    public static void main(String[] args) throws InterruptedException {
        MessageWrapperEncoder messageWrapperEncoder=new MessageWrapperEncoder();

        for(int i=0;i<10;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bootstrap bootstrap = new Bootstrap();
                    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                    bootstrap.group(bossGroup).channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(
                                            new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[]{0x7e})));

                                    pipeline.addLast(new MessageFixDecoder());
                                    pipeline.addLast(new MessageFixEncoder());

                                    pipeline.addLast(new MessageWrapperDecoder());
                                    pipeline.addLast(new PackageAggregator());
                                    pipeline.addLast(messageWrapperEncoder);
                                    pipeline.addLast(new EchoHandler());
                                }
                            });

                    ChannelFuture channelFuture = null;
                    try {
                        channelFuture = bootstrap.connect(NettyConstant.REMOTEIP, NettyConstant.PORT).sync();
                        channelFuture.channel().closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            ).start();
        }

    }
}
