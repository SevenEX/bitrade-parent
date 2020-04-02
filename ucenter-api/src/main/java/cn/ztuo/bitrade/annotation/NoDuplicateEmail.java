package cn.ztuo.bitrade.annotation;

import cn.ztuo.bitrade.validation.NoDuplicateEmailValidator;
import cn.ztuo.bitrade.validation.NoDuplicatePhoneValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoDuplicateEmailValidator.class)
public @interface NoDuplicateEmail {

    String message() default "{EMAIL_ALREADY_BOUND}";;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}