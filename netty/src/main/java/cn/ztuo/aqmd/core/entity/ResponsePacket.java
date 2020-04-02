/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: ResponsePacket.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月19日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月19日, Create
 */
package cn.ztuo.aqmd.core.entity;

/**
 * <p>Title: ResponsePacket</p>
 * <p>Description: </p>
 * 响应数据包
 * @author MrGao
 * @date 2019年7月19日
 */
public class ResponsePacket extends Packet {
	protected final static int HEADER_LENGTH = MIN_LENGTH + 4;
	// 响应码。4字节
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public int getHeaderLength() {
        return HEADER_LENGTH;
    }
}
