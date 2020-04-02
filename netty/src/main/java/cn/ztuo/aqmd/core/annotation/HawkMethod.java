/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkMethod.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019年7月18日
 * @version 1.0
 * History:
 * v1.0.0, , 2019年7月18日, Create
 */
package cn.ztuo.aqmd.core.annotation;

import java.lang.annotation.*;

/**
 * <p>Title: HawkMethod</p>
 * <p>Description: </p>
 * 使用该注解对服务方法进行标注
 * @author MrGao
 * @date 2019年7月18日
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HawkMethod {
	/**
     * 指令号
     * @return
     */
    int cmd();

    /**
     * 指令版本号
     * @return
     */
    byte version() default 1;

    /**
     * 服务方法是否已经过期，默认不过期
     */
    ObsoletedType obsoleted() default ObsoletedType.NO;
}
