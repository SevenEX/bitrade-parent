package cn.ztuo.bitrade.aspect;

import cn.ztuo.bitrade.annotation.Limits;
import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberApiKey;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberApiKeyService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedissonUtil;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 权限限制验证切面
 *
 * @author lvlingling
 * @version 2020-1-26 14:13
 */
@Aspect
@Component
@Slf4j
@Order(1)
public class LimitsAspect {

    @Autowired
    private MemberApiKeyService apiKeyService;
    @Autowired
    protected LocaleMessageSourceService msService;

    public LimitsAspect() {
        log.info("权限验证 AOP ");
    }

    @Around(value = "@annotation(cn.ztuo.bitrade.annotation.Limits) && @annotation(limits)",
            argNames = "joinPoint, limits")
    public Object authLogic(ProceedingJoinPoint joinPoint, Limits limits) throws Throwable {
        if (StringUtils.isEmpty(limits.value())) {
            log.error("错误: 权限验证切面必须指定key!");
            return joinPoint.proceed();
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("错误: 权限验证切面必须有Request上下文!");
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String ac = request.getParameter("accessKeyId");
        MemberApiKey memberApiKey = apiKeyService.findMemberApiKeyByApiKey(ac);
        if (memberApiKey == null || StringUtils.isEmpty(memberApiKey.getPowerLimit())) {
            return MessageResult.error(500, msService.getMessage("PERMISSION_DENIED"));
        }else if (!Splitter.on(',').splitToList(memberApiKey.getPowerLimit()).contains(limits.value())){
            return MessageResult.error(500, msService.getMessage("PERMISSION_DENIED"));
        }
        return joinPoint.proceed();
    }
}