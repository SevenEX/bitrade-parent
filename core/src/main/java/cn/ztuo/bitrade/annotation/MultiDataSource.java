package cn.ztuo.bitrade.annotation;

import java.lang.annotation.*;

/**
 * <p>多数据源标识</p>
 * <p>使用方式：必须用在方法上</p>
 *
 * @author maxzhao
 * @date 2019-06-26 16:13
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MultiDataSource {
    String name() default "main";
}