/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: ObsoletedType.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月18日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月18日, Create
 */
package cn.ztuo.aqmd.core.annotation;

/**
 * <p>Title: ObsoletedType</p>
 * <p>Description: </p>
 * 服务方法是否已经过期，过期的服务方法不能再访问
 * @author MrGao
 * @date 2019年7月18日
 */
public enum ObsoletedType {
	 YES, NO;

    public static boolean isObsoleted(ObsoletedType type) {
        if (YES == type) {
            return true;
        } else {
            return false;
        }
    }
}
