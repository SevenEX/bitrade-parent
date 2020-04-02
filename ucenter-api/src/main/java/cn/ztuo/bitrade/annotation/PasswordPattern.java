package cn.ztuo.bitrade.annotation;

import cn.ztuo.bitrade.validation.PasswordPatternValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordPatternValidator.class)
public @interface PasswordPattern {

    String message() default "{PASSWORD_LENGTH_ILLEGAL}";;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}