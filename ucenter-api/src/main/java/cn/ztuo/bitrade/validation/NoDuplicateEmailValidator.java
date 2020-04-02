package cn.ztuo.bitrade.validation;

import cn.ztuo.bitrade.annotation.NoDuplicateEmail;
import cn.ztuo.bitrade.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoDuplicateEmailValidator implements ConstraintValidator<NoDuplicateEmail, String> {
    @Autowired
    private MemberService memberService;
    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return !memberService.emailIsExist(email);
    }
}
