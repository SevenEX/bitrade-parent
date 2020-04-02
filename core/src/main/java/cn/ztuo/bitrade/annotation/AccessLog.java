package cn.ztuo.bitrade.annotation;

import cn.ztuo.bitrade.constant.AdminModule;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLog {
    String operation();
    AdminModule module();
}

