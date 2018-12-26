package com.bulingbuu.bubprotocol.handler;

import com.bulingbuu.bubprotocol.bean.Message;
import com.bulingbuu.bubprotocol.bean.PackageData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bulingbuu
 * @date 18-12-17 下午4:49
 * <p>
 * 分包聚合
 */
@Slf4j
public class PackageAggregator extends MessageToMessageDecoder<Message> {

    private Map<Short, PackageBuf> bufMap = new HashMap<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        if (!msg.isPkg()) {
            PackageData data = new PackageData();
            data.setTarget(msg.getTarget());
            data.setEncryp(msg.getEncryp());
            data.setMsgId(msg.getMsgId());
            data.setSource(msg.getSource());
            data.setBody(msg.getBody());

            out.add(data);
            return;
        }
        log.info("分包聚合");
        short msgNum = msg.getMsgNum();
        short pkgNum = msg.getPkgNum();
        short msgIndexNum = (short) (msgNum - pkgNum + 1);

        //不属于重传包
        if (!bufMap.containsKey(msgIndexNum)) {
            PackageBuf packageBuf = new PackageBuf(msg.getPkgSize());
            packageBuf.addMessage(msg);
            bufMap.put(msgIndexNum, packageBuf);
        } else {
            PackageBuf packageBuf = bufMap.get(msgIndexNum);
            int ret = packageBuf.addMessage(msg);
            if (ret == 0) {
                bufMap.remove(msgIndexNum);

                byte[] bytes = packageBuf.getBufArray();

                PackageData data = new PackageData();
                data.setTarget(msg.getTarget());
                data.setEncryp(msg.getEncryp());
                data.setMsgId(msg.getMsgId());
                data.setSource(msg.getSource());
                data.setBody(bytes);

                out.add(data);
            } else if (ret > 0) {
                log.info("发起重传指令");
            }
        }

    }

    private class PackageBuf {
        private int maxSize;
        private int lastNum;
        private int currentSize;
        private int index;

        private CompositeByteBuf byteBufs;

        public PackageBuf(int maxSize) {
            this.maxSize = maxSize;
            byteBufs = Unpooled.compositeBuffer(maxSize);
        }

        /**
         * @param message
         * @return
         */
        public int addMessage(Message message) {
            int pkgNum = message.getPkgNum();

            ByteBuf body = Unpooled.wrappedBuffer(message.getBody());
            if (pkgNum > lastNum) {
                //顺序接收
                byteBufs.addComponent(true, body);
            } else if (pkgNum < lastNum) {
                int i = 0;
                int j = 0;
                while (i < pkgNum - 1) {
                    if ((index & (1 << i)) == 0) {
                        //前面包的个数
                        j++;
                    }
                    i++;
                }

                byteBufs.addComponent(true, pkgNum - j - 1, body);
            } else {
                log.info("包序号相同,不处理");
                return -2;
            }

            currentSize++;
            if (currentSize == maxSize) {
                //接收完成
                return 0;
            }
            if (pkgNum == maxSize) {
                //接收到最后一个包
                return index;
            }
            lastNum = pkgNum;
            index |= (1 << pkgNum);

            return -1;
        }

        public byte[] getBufArray() {
            byte[] bytes = new byte[byteBufs.readableBytes()];
            byteBufs.readBytes(bytes);
            return bytes;
        }
    }

}
