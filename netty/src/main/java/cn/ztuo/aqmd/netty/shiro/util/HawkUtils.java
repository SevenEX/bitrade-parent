/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkUtils.java</p>
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
 * <p>Title: HawkUtils</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019年7月25日
 */
public class HawkUtils {
	public static RequestPacket getRequest(Object requestPairSource) {
        if (requestPairSource instanceof RequestPairSource) {
            return ((RequestPairSource) requestPairSource).getHawkRequest();
        }
        return null;
    }

    public static ResponsePacket getResponse(Object requestPairSource) {
        if (requestPairSource instanceof RequestPairSource) {
            return ((RequestPairSource) requestPairSource).getHawkResponse();
        }
        return null;
    }
    public static boolean _isSessionCreationEnabled(Object requestPairSource) {
        if (requestPairSource instanceof RequestPairSource) {
            RequestPairSource source = (RequestPairSource) requestPairSource;
            return _isSessionCreationEnabled(source.getHawkRequest());
        }
        return true; //by default
    }
}
