/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: RequestPairSource.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月25日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月25日, Create
 */
package cn.ztuo.aqmd.netty.shiro.util;


import cn.ztuo.aqmd.core.entity.RequestPacket;
import cn.ztuo.aqmd.core.entity.ResponsePacket;

/**
 * <p>Title: RequestPairSource</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019年7月25日
 */
public interface RequestPairSource {

	RequestPacket getHawkRequest();

	ResponsePacket getHawkResponse();
}
