package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.bean.Message;
import com.bulingbuu.bubprotocol.constant.NettyConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author bulingbuu
 * @date 18-12-17 下午2:39
 * <p>
 * 解析消息
 */
@Slf4j
public class MessageWrapperDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        short msgId = in.readShort();
        short bodyAttr = in.readShort();

        short bodyLen = (short) (bodyAttr & 0x03FF);
        short encryp = (short) ((bodyAttr & 0x0C00) >> 10);
        boolean isPkg = (bodyAttr & 0x2000) != 0;

        int source = in.readInt();
        byte[] targetByte = new byte[NettyConstant.TARGET_LENGTH];
        in.readBytes(targetByte);

        short msgNum = in.readShort();

        Message.MessageBuilder builder = Message.builder()
                .msgId(msgId)
                .msgNum(msgNum)
                .encryp(encryp)
                .source(source)
                .target(new String(targetByte))
                .bodyLen(bodyLen);

        if (isPkg) {
            short pgkSize = in.readShort();
            short pkgNum = in.readShort();
            builder.pkgSize(pgkSize).pkgNum(pkgNum).isPkg(true);
        }

        byte[] body = new byte[bodyLen];
        in.readBytes(body);

        builder.body(body);

        out.add(builder.build());
    }
}
