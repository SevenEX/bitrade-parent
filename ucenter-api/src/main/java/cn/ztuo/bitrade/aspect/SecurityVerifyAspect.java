package cn.ztuo.bitrade.aspect;

import cn.ztuo.bitrade.annotation.SecurityVerification;
import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.enums.VerificationType;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MaskUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedissonUtil;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * 安全验证切面
 *
 * @author Zane
 * @version 2019-12-11 15:22
 */
@Aspect
@Component
@Slf4j
@Order(4)
public class SecurityVerifyAspect {

    @Autowired
    private MemberService memberService;
    @Autowired
    protected LocaleMessageSourceService msService;

    public SecurityVerifyAspect() {
        log.info("安全验证 AOP ");
    }

    @Around(value = "@annotation(cn.ztuo.bitrade.annotation.SecurityVerification) && @annotation(securityVerification)",
            argNames = "joinPoint, securityVerification")
    public Object authLogic(ProceedingJoinPoint joinPoint, SecurityVerification securityVerification) throws Throwable {
        if (StringUtils.isEmpty(securityVerification.value())) {
            log.error("错误: 安全验证切面必须指定key!");
            return joinPoint.proceed();
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("错误: 安全验证切面必须有Request上下文!");
            return joinPoint.proceed();
        }
//        MessageResult messageResult = checkValidResult(joinPoint);
//        if (messageResult != null) {
//            return messageResult;
//        }
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = attributes.getRequest().getSession(true);
        AuthMember user = (AuthMember) session.getAttribute(SysConstant.SESSION_MEMBER);
        String account = getParameterField(joinPoint, request, securityVerification.accountKey());
        String token = getParameterField(joinPoint, request, securityVerification.tokenKey());
        if (securityVerification.isRegister()) {
            if (StringUtils.isEmpty(token)) {
                return getRegisterError(account, securityVerification.verificationType());
            }
            RMapCache<String, String> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.SECURITY_VERIFY + securityVerification.value());
            if (StringUtils.equals(mapCache.get(account), token)) {
                return joinPoint.proceed();
            }
            return getRegisterError(account, securityVerification.verificationType());
        } else {
            Member member;
            if (user == null) {
                if (StringUtils.isEmpty(account)) {
                    return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXITS"));
                }
                member = memberService.findByPhoneOrEmail(account);
            } else {
                member = memberService.findOne(user.getId());
            }
            if(member == null) {
                return MessageResult.error(4000, msService.getMessage("ACCOUNT_NOT_EXITS"));
            }
            if (StringUtils.isEmpty(token)) {
                return getSecurityMessageResult(member, securityVerification.value());
            }
            RMapCache<String, String> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.SECURITY_VERIFY + securityVerification.value());
            if (StringUtils.equals(mapCache.get(String.valueOf(member.getId())), token)) {
                return joinPoint.proceed();
            }
            return getSecurityMessageResult(member, securityVerification.value());
        }
    }

    private MessageResult checkValidResult(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (null != args && args.length > 0) {
            for (Object obj : args) {
                if (obj instanceof BindingResult) {
                    // 参数验证
                    MessageResult messageResult = BindingResultUtil.validate((BindingResult) obj);
                    if (messageResult != null) {
                        return messageResult;
                    }
                    break;
                }
            }
        }
        return null;
    }

    private MessageResult getRegisterError(String account, VerificationType verificationType) {
        HashMap<String, Object> map = new HashMap<>();
        switch (verificationType) {
            case Phone:
                map.put("type", SysConstant.TOKEN_REGISTER_PHONE);
                map.put("phone", 1);
                map.put("phoneName", account);
                break;
            case Email:
                map.put("type", SysConstant.TOKEN_REGISTER_MAIL);
                map.put("email", 1);
                map.put("emailName", account);
                break;
            default:
                break;
        }
        MessageResult messageResult = MessageResult.error(403, msService.getMessage("INVALID_TOKEN"));
        messageResult.setData(map);
        return messageResult;
    }

    private MessageResult getSecurityMessageResult(Member member, String tokenType) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", tokenType);
        map.put("email", member.getEmailState());
        if (member.getEmailState() != 0) {
            map.put("emailName", MaskUtil.maskEmail(member.getEmail()));
        }
        map.put("phone", member.getPhoneState());
        if (member.getPhoneState() != 0) {
            map.put("phoneName", MaskUtil.maskMobile(member.getMobilePhone()));
        }
        map.put("google", member.getGoogleState());
        MessageResult messageResult = MessageResult.error(403, msService.getMessage("INVALID_TOKEN"));
        messageResult.setData(map);
        return messageResult;
    }

    private String getParameterField(ProceedingJoinPoint joinPoint, HttpServletRequest request, String parameterName) {
        String paramValue = request.getParameter(parameterName);
        if (StringUtils.isEmpty(paramValue) && joinPoint.getArgs().length > 0) {
            if (joinPoint.getArgs().length > 0) {
                if (joinPoint.getArgs()[0] instanceof String) {
                    paramValue = (String) joinPoint.getArgs()[0];
                } else {
                    if (joinPoint.getArgs()[0] != null) {
                        Field field = ReflectionUtils.findField(joinPoint.getArgs()[0].getClass(), CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, parameterName));
                        if (field != null) {
                            paramValue = (String) ReflectionUtils.getField(field, joinPoint.getArgs()[0]);
                        }
                    }
                }
            }
        }
        return paramValue;
    }
}