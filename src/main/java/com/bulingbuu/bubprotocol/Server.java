package com.bulingbuu.bubprotocol;

import com.bulingbuu.bubprotocol.constant.NettyConstant;
import com.bulingbuu.bubprotocol.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author bulingbuu
 * @date 18-12-26 下午4:17
 */
@Slf4j
public class Server {
    public static void main(String[] args) {

        new Server().bind();
    }

    public void bind() {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        ServerBootstrap b = new ServerBootstrap();

        MessageWrapperEncoder messageWrapperEncoder=new MessageWrapperEncoder();
        MessageFixEncoder messageFixEncoder=new MessageFixEncoder();
        MessageDispatchHandler messageDispatchHandler=new MessageDispatchHandler();
        try {
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(
                                    new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[]{0x7e})));

                            pipeline.addLast(new MessageFixDecoder());
                            pipeline.addLast(messageFixEncoder);

                            pipeline.addLast(new MessageWrapperDecoder());
                            pipeline.addLast(new PackageAggregator());
                            pipeline.addLast(messageWrapperEncoder);
                            pipeline.addLast(messageDispatchHandler);
                        }
                    });

            // 绑定端口，同步等待成功
            ChannelFuture channelFuture = b.bind(NettyConstant.REMOTEIP, NettyConstant.PORT).sync();
            log.info("Netty server start ok : "
                    + (NettyConstant.REMOTEIP + " : " + NettyConstant.PORT));

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("netty 关闭");
        }

    }
}
