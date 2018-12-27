package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.constant.NettyConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author bulingbuu
 * @date 18-12-26 下午2:10
 * <p>
 * 转义还原.验证校验码
 */
@Slf4j
public class MessageFixDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readableBytes();
        if (length <= 0) {
            return;
        }
        byte[] bs;
        if (false) {
            bs = in.array();
        } else {
            bs = new byte[in.readableBytes()];
            in.readBytes(bs);
        }
        ByteBuf byteBuf = Unpooled.buffer();

        byte checkByte = 0;
        byte oldCheckByte = bs[bs.length - 1];
        for (int i = 0; i < bs.length - 1; i++) {
            byte b = bs[i];

            if (b == NettyConstant.byte_7d) {
                i++;
                b = bs[i];

                if (b == NettyConstant.byte_01) {
                    //校验位是特殊字符
                    if (i == bs.length - 1) {
                        oldCheckByte = NettyConstant.byte_7d;
                    } else {
                        byteBuf.writeByte(NettyConstant.byte_7d);
                        checkByte ^= NettyConstant.byte_7d;
                    }
                } else if (b == NettyConstant.byte_02) {
                    if (i == bs.length - 1) {
                        oldCheckByte = NettyConstant.byte_7e;
                    } else {
                        byteBuf.writeByte(NettyConstant.byte_7e);
                        checkByte ^= NettyConstant.byte_7e;
                    }
                } else {
                    log.error("有错");
                    return;
                }
                continue;
            }
            checkByte ^= b;
            byteBuf.writeByte(b);
        }
        if (checkByte != oldCheckByte) {
            log.error("检验码不通过");
            return;
        }

        out.add(byteBuf);
    }
}
