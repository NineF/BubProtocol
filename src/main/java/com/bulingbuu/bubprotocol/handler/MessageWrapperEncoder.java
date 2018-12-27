package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.bean.PackageData;
import com.bulingbuu.bubprotocol.constant.NettyConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author bulingbuu
 * @date 18-12-14 下午2:17
 * <p>
 * 消息包装.分包
 * 流水号,消息体属性等
 * <p>
 * 确保线程安全
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageWrapperEncoder extends MessageToMessageEncoder<PackageData> {

    private AtomicInteger num = new AtomicInteger(1);

    @Override
    protected void encode(ChannelHandlerContext ctx, PackageData msg, List<Object> out) throws Exception {
//        log.info("开始包装消息");
        List<byte[]> bytes = wrapperMsg(msg);
        log.info("封装结束,消息ID={},消息总包数={}", msg.getMsgId(), bytes.size());
        out.addAll(bytes);
    }

    /**
     * 封装消息
     *
     * @param msg
     * @return
     */
    private List<byte[]> wrapperMsg(PackageData msg) {
        List<byte[]> list = new ArrayList<>();
        byte[] body = msg.getBody();
        int bodyLen = body.length;
        short msgId = msg.getMsgId();
        String target = msg.getTarget();
        byte[] targetByte;
        //长度不足用0代替
        if (target.length() < NettyConstant.TARGET_LENGTH) {
            targetByte = concatAll(new byte[NettyConstant.TARGET_LENGTH - target.length()], target.getBytes(Charset.forName("UTF-8")));
        } else {
            targetByte = target.getBytes(Charset.forName("UTF-8"));
        }

        int source = msg.getSource();
        short encryp = msg.getEncryp();
        //没有分包
        if (bodyLen < NettyConstant.MAX_BODY_LENGTH) {
            ByteBuf byteBuf = Unpooled.buffer();
            //消息id
            byteBuf.writeShort(msgId);
            //设置消息体长度
            short bodyAttr = (short) (bodyLen & 0x03FF);
            //设置加密方式
            bodyAttr |= (encryp << 10);
            //整理
            bodyAttr &= 0x1FFF;
            //消息体属性
            byteBuf.writeShort(bodyAttr);
            //消息来源
            byteBuf.writeInt(source);
            //消息目标
            byteBuf.writeBytes(targetByte);
            //消息流水号
            byteBuf.writeShort(num.addAndGet(1));
            //消息体
            byteBuf.writeBytes(msg.getBody());

            list.add(byteBufToBytes(byteBuf));

            return list;
        }
        //分包
        short pkgSize = (short) (bodyLen / NettyConstant.MAX_BODY_LENGTH + 1);
        short lastBodyLen = (short) (bodyLen % NettyConstant.MAX_BODY_LENGTH);
        short msgNum = (short) num.getAndAdd(pkgSize);


        for (int i = 1; i < pkgSize; i++) {
            ByteBuf byteBuf = Unpooled.buffer();
            //消息id
            byteBuf.writeShort(msgId);
            //设置消息体长度
            short bodyAttr = (short) (NettyConstant.MAX_BODY_LENGTH & 0x03FF);
            //设置加密方式
            bodyAttr |= (encryp << 10);
            //分包
            bodyAttr |= 0x3000;
            //整理
            bodyAttr &= 0x3FFF;
            //消息体属性
            byteBuf.writeShort(bodyAttr);
            //消息来源
            byteBuf.writeInt(source);
            //消息接收者
            byteBuf.writeBytes(targetByte);
            //消息流水号
            byteBuf.writeShort(msgNum++);
            //总包数
            byteBuf.writeShort(pkgSize);
            //包序号
            byteBuf.writeShort(i);
            //消息体
            short startIndex = (short) ((i - 1) * NettyConstant.MAX_BODY_LENGTH);

            byteBuf.writeBytes(msg.getBody(), startIndex, NettyConstant.MAX_BODY_LENGTH);

            list.add(byteBufToBytes(byteBuf));

        }

        ByteBuf byteBuf = Unpooled.buffer();
        //消息id
        byteBuf.writeShort(msgId);
        //设置消息体长度
        short bodyAttr = (short) (lastBodyLen & 0x03FF);
        //设置加密方式
        bodyAttr |= (encryp << 10);
        //分包
        bodyAttr |= 0x3000;
        //整理
        bodyAttr &= 0x3FFF;
        //消息体属性
        byteBuf.writeShort(bodyAttr);
        //消息来源
        byteBuf.writeInt(source);
        //消息接收者
        byteBuf.writeBytes(targetByte);
        //消息流水号
        byteBuf.writeShort(msgNum++);
        //总包数
        byteBuf.writeShort(pkgSize);
        //包序号
        byteBuf.writeShort(pkgSize);
        //消息体
        short startIndex = (short) ((pkgSize - 1) * NettyConstant.MAX_BODY_LENGTH);
        byteBuf.writeBytes(msg.getBody(), startIndex, lastBodyLen);

        list.add(byteBufToBytes(byteBuf));

        return list;
    }

    private byte[] byteBufToBytes(ByteBuf byteBuf) {
        if (false) {
            return byteBuf.array();
        } else {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            return bytes;
        }
    }

    public byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }
}