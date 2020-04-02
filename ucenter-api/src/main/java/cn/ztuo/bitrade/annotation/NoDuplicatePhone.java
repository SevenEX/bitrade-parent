package cn.ztuo.bitrade.annotation;

import cn.ztuo.bitrade.validation.NoDuplicatePhoneValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoDuplicatePhoneValidator.class)
public @interface NoDuplicatePhone {

    String message() default "{PHONE_ALREADY_EXISTS}";;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}