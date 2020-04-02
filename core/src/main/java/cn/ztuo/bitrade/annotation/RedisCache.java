package cn.ztuo.bitrade.annotation;

import cn.ztuo.bitrade.cache.DefaultMethodParamSerializer;
import cn.ztuo.bitrade.cache.MethodParamSerializer;

import java.lang.annotation.*;

/**
 * <p>Redis缓存切面</p>
 * <p>使用方式：必须用在方法上</p>
 *
 * @author Zane
 * @version 2019-12-03 16:13
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RedisCache {
    /**
     * @return Redis Key，存放缓存数据的位置
     */
    String value();

    /**
     * @return 缓存刷新周期，单位秒
     */
    int ttl() default 600;

    /**
     * @return 是否忽略方法参数
     */
    boolean ignoreParam() default true;

    /**
     * @return 参数序列化器，默认将全部参数转JSON作为Key
     */
    Class<? extends MethodParamSerializer> paramSerializer() default DefaultMethodParamSerializer.class;
}