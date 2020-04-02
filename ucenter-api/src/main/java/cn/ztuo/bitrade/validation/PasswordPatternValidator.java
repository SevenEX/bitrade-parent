package cn.ztuo.bitrade.validation;

import cn.ztuo.bitrade.annotation.PasswordPattern;
import cn.ztuo.bitrade.utils.ValidateUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordPatternValidator implements ConstraintValidator<PasswordPattern, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if(StringUtils.isEmpty(password))
            return false;
        return ValidateUtils.validatePassword(password);
    }
}
