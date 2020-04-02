/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: FilterChain.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月28日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月28日, Create
 */
package cn.ztuo.aqmd.core.filter;

import cn.ztuo.aqmd.core.entity.RequestPacket;
import cn.ztuo.aqmd.core.entity.ResponsePacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>Title: FilterChain</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019年7月28日
 */
public interface FilterChain {
	
	public void doFilter(RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx);
}
