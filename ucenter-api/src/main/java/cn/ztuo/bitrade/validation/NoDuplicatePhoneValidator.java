package cn.ztuo.bitrade.validation;

import cn.ztuo.bitrade.annotation.NoDuplicatePhone;
import cn.ztuo.bitrade.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoDuplicatePhoneValidator implements ConstraintValidator<NoDuplicatePhone, String> {
    @Autowired
    private MemberService memberService;
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext constraintValidatorContext) {
        return !memberService.phoneIsExist(phone);
    }
}
