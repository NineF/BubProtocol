package com.bulingbuu.bubprotocol.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author bulingbuu
 * @date 18-12-26 上午9:42
 * <p>
 * kafka通用消息实体
 */
@Setter
@Getter
@ToString
public class PackageData {
    /**
     * 消息id
     */
    private short msgId;
    /**
     * 消息来源
     */
    private int source;
    /**
     * 加密方式
     */
    private short encryp;
    /**
     * 目标设备
     */
    private String target;
    /**
     * 消息体
     */
    private byte[] body;
}
