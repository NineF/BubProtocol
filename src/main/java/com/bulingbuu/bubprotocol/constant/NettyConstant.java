package com.bulingbuu.bubprotocol.constant;

public final class NettyConstant {
    public static final String REMOTEIP = "127.0.0.1";
    public static final int PORT = 10138;
    /**
     * 消息体最大长度
     */
    public static final int MAX_BODY_LENGTH = 4;

    /**
     * 目标长度
     */
    public static final int TARGET_LENGTH = 12;

    public static final byte IDENTIFIER = 0x7e;
    public static final byte byte_7e = 0x7e;
    public static final byte byte_7d = 0x7d;
    public static final short byte_ret_7d = 0x7d01;
    public static final short byte_ret_7e = 0x7d02;

    public static final byte byte_01 = 0x01;
    public static final byte byte_02 = 0x02;
}
