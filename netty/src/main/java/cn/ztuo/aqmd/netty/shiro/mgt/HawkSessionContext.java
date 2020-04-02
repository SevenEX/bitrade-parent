/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkSessionContext.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月25日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月25日, Create
 */
package cn.ztuo.aqmd.netty.shiro.mgt;

import cn.ztuo.aqmd.core.entity.RequestPacket;
import cn.ztuo.aqmd.core.entity.ResponsePacket;
import cn.ztuo.aqmd.netty.shiro.util.RequestPairSource;
import org.apache.shiro.session.mgt.SessionContext;

/**
 * <p>Title: HawkSessionContext</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019年7月25日
 */
public interface HawkSessionContext extends SessionContext,RequestPairSource {
	 RequestPacket getHawkRequest();
	 void setHawkRequest(RequestPacket request);
	 ResponsePacket getHawkResponse();
	 void setHawkResponse(ResponsePacket response);
}
