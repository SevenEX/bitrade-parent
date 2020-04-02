package cn.ztuo.bitrade.annotation;

import cn.ztuo.bitrade.enums.VerificationType;

import java.lang.annotation.*;

/**
 * 安全验证切面
 *
 * @author Zane
 * @version 2019-12-11 14:13
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SecurityVerification {
    /**
     * @return Key，要求的权限验证类型（使用Redis Key）
     */
    String value();

    /**
     * @return 账户Key，账户名参数名，优先取请求url参数，其次取函数第一参数（如果是Dto，则反射取值）
     */
    String accountKey() default "account";

    /**
     * @return 令牌Key，优先取请求url参数，其次取函数第一参数（如果是Dto，则反射取值）
     */
    String tokenKey() default "token";

    boolean isRegister() default false;

    VerificationType verificationType() default VerificationType.Auto;
}