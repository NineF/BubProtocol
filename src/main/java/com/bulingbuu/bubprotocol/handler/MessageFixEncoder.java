package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.constant.NettyConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author bulingbuu
 * @date 18-12-26 上午11:56
 * 消息 计算.填充.转义
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageFixEncoder extends MessageToByteEncoder<byte[]> {
    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        encodeNewByte(msg, out);
    }

    private void encodeNewByte(byte[] bytes, ByteBuf byteBuf) {
        byte checkByte = 0;

        byteBuf.writeByte(NettyConstant.IDENTIFIER);
        for (byte b : bytes) {
            if (b == NettyConstant.byte_7d) {
                byteBuf.writeShort(NettyConstant.byte_ret_7d);
                checkByte ^= NettyConstant.byte_7d;
            } else if (b == NettyConstant.byte_7e) {
                byteBuf.writeShort(NettyConstant.byte_ret_7e);
                checkByte ^= NettyConstant.byte_7e;
            } else {
                byteBuf.writeByte(b);
                checkByte ^= b;
            }

        }

        byteBuf.writeByte(checkByte);
        byteBuf.writeByte(NettyConstant.IDENTIFIER);
    }
}
