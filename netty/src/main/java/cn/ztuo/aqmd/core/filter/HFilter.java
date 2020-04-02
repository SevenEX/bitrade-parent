/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: Hfilter.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月28日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月28日, Create
 */
package cn.ztuo.aqmd.core.filter;

import cn.ztuo.aqmd.core.annotation.HawkFilter;
import cn.ztuo.aqmd.core.entity.RequestPacket;
import cn.ztuo.aqmd.core.entity.ResponsePacket;
import cn.ztuo.aqmd.core.exception.NettyException;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * <p>Title: Hfilter</p>
 * <p>Description: </p>
 * @Author MrGao
 * @Date 2019年7月28日
 */
public abstract class HFilter {
	
	public abstract void init() throws NettyException;
	public abstract void doFilter (RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx, FilterChain chain )
			 throws IOException, NettyException;
	public abstract void destroy();

	protected String buildExceptionMsg(int code,String message){
		return code+"~"+message;
	}
	/**
     * 是否拦截该指令
     * 返回true表示进行拦截
     * @param req 请求包
     * @return 是否匹配
     */
    public boolean isMatch(RequestPacket req) {
    	HawkFilter hawkFilter = this.getClass().getAnnotation(HawkFilter.class);
        for (int cmd : hawkFilter.ignoreCmds()) {
            if (cmd == req.getCmd()) {
                return false;
            }
        }
        for (int cmd : hawkFilter.cmds()) {
            if (cmd == req.getCmd()) {
                return true;
            }
        }
        return true;
    }
}
