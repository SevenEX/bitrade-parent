package cn.ztuo.bitrade.annotation;

import java.lang.annotation.*;

/**
 * 权限限制验证切面
 *
 * @author lvlingling
 * @version 2020-1-26 14:13
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Limits {

    String value();

}