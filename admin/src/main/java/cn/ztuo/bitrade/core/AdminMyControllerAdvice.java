package cn.ztuo.bitrade.core;

import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.MessageResult;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Seven
 * @date 2019年12月22日
 */
@ControllerAdvice
public class AdminMyControllerAdvice {
    @Autowired
    private LocaleMessageSourceService messageSource;
    /**
     * 拦截捕捉无权限异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = AuthorizationException.class)
    public MessageResult handleAuthorizationError(AuthorizationException ex) {
        ex.printStackTrace();
        MessageResult result = MessageResult.error(5000, messageSource.getMessage("PERMISSION_DENIED"));
        return result;
    }

    @ResponseBody
    @ExceptionHandler({AuthenticationException.class,UnauthenticatedException.class})
    public MessageResult handleAuthenticationError(AuthorizationException ex) {
        ex.printStackTrace();
        MessageResult result = MessageResult.error(4000, messageSource.getMessage("UNAUTHENTICATED"));
        return result;
    }
}
