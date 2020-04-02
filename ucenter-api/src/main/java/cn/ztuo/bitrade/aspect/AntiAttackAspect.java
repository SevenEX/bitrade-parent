package cn.ztuo.bitrade.aspect;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 登录之后发送邮件或者短信频率最快也只能一分钟一次
 *
 * @author Seven
 * @date 2019年04月03日
 */
@Aspect
@Component
@Slf4j
@Order(2)
public class AntiAttackAspect {
    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    @Pointcut("execution(public * cn.ztuo.bitrade.controller.RegisterController.sendBindEmail(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.VerifyController.emailCode(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.VerifyController.regEmailCode(..))")
    public void antiAttackMail() {
    }

    @Pointcut("execution(public * cn.ztuo.bitrade.controller.VerifyController.smsCode(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.VerifyController.regSmsCode(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.SmsController.setBindPhoneCode(..))")
    public void antiAttack() {
    }

    @Before("antiAttack()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        check(joinPoint, "sms");
    }

    @Before("antiAttackMail()")
    public void doBeforeMail(JoinPoint joinPoint) throws Throwable {
        check(joinPoint, "mail");
    }

    public void check(JoinPoint joinPoint, String type) throws Exception {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId() + type;
        Object code = redisUtil.get(key);
        if (code != null) {
            throw new IllegalArgumentException(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
        }
    }

    @AfterReturning(pointcut = "antiAttack()")
    public void doAfterReturning() throws Throwable {
        afterReturn("sms");
    }

    @AfterReturning(pointcut = "antiAttackMail()")
    public void doAfterReturningMail() throws Throwable {
        afterReturn("mail");
    }

    private void afterReturn(String type) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        log.info("======验证码频次拦截========" + request.getRemoteHost());
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId() + type;
        redisUtil.set(key, "send sms all too often", 1, TimeUnit.MINUTES);
    }

}
