/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: Dispatcher.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月19日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月19日, Create
 */
package cn.ztuo.aqmd.core;

import cn.ztuo.aqmd.core.entity.Packet;
import cn.ztuo.aqmd.core.exception.NettyException;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>Title: Dispatcher</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019年7月19日
 */
public interface Dispatcher <R extends Packet, P extends Packet> {

    /**
     * 分发请求
     * @param request 请求包
     * @param ctx Context。目的是为了下层业务代码取到
     * @return
     * @throws
     */
    P dispatch(R request, ChannelHandlerContext ctx) throws NettyException;
}
