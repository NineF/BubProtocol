package com.bulingbuu.bubprotocol.bean;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author bulingbuu
 * @date 18-12-14 上午11:17
 */
@Getter
@ToString
@Builder
public class Message {
    /**
     * 消息id
     */
    private short msgId;
    /**
     * 消息流水号
     */
    private short msgNum;
    /**
     * 消息体长度
     */
    private short bodyLen;
    /**
     * 消息来源
     */
    private int source;
    /**
     * 消息体加密方式
     */
    private short encryp;
    /**
     * 消息目标
     */
    private String target;
    /**
     * 是否分包
     */
    private boolean isPkg;
    /**
     * 包序号(当isPkg为true)
     */
    private short pkgNum;
    /**
     * 包数量(当isPkg为true)
     */
    private short pkgSize;
    /**
     * 消息体
     */
    private byte[] body;
}
