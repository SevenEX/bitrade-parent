package cn.ztuo.aqmd.netty.codec;

import io.netty.channel.Channel;

/**
 * 
 * <p>Title: Codec</p>
 * <p>Description: </p>
 * 加解密接口
 * @author MrGao
 * @date 2019年6月26日
 */
public interface  Codec {

    /**
     * 将报文body部分解密
     * @param channel io连接通道信息
     * @param body
     * @return
     */
    byte[] decrypt(Channel channel, byte[] body);

    /**
     * 将响应body部分加密
     * @param channel io连接通道信息
     * @param body
     * @return
     */
    byte[] encrypt(Channel channel, byte[] body);
}
