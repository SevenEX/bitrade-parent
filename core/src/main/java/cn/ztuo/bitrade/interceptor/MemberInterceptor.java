package cn.ztuo.bitrade.interceptor;


import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.event.MemberEvent;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.util.RequestUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

/**
 * @author Seven
 * @date 2019年01月11日
 */
@Slf4j
public class MemberInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        log.info(request.getRequestURL().toString());
        AuthMember user = (AuthMember) session.getAttribute(SysConstant.SESSION_MEMBER);
        if (user != null) {
            return true;
        } else {
            String host = request.getHeader("x-auth-host");
            if(StringUtils.isEmpty(host)){
                host =  RequestUtil.remoteWay(request);
            }
            String token = request.getHeader("access-auth-token");
            log.info("access-auth-token:{}",token);
            if(StringUtils.isEmpty(token)){
                token = request.getHeader("x-auth-token");
                log.info("x-auth-token={}",token);
            }

            //解决service为null无法注入问题
            BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            MemberService memberService = (MemberService) factory.getBean("memberService");
            MemberEvent memberEvent = (MemberEvent) factory.getBean("memberEvent");
            LocaleMessageSourceService msService = (LocaleMessageSourceService) factory.getBean("localeMessageSourceService");
            Member member = memberService.loginWithToken(token, host);
            if (member != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, 24 * 7);
                member.setTokenExpireTime(calendar.getTime());
                memberService.save(member);
                memberEvent.onLoginSuccess(member, request.getRemoteAddr());
                session.setAttribute(SysConstant.SESSION_MEMBER, AuthMember.toAuthMember(member));
                return true;
            } else {
                ajaxReturn(response, 4000, msService.getMessage("RE_LOGIN"));
                return false;
            }
        }
    }


    public static void ajaxReturn(HttpServletResponse response, int code, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("errCode", code);
        json.put("message", msg);
        out.print(json.toString());
        out.flush();
        out.close();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

    }
}
