package cn.ztuo.bitrade.system;

import cn.ztuo.bitrade.exception.GeeTestException;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author Seven
 * @date 2019年12月23日
 */
@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {
    @Autowired
    private LocaleMessageSourceService msService;
    /**
     * 拦截乐观锁失败异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = ObjectOptimisticLockingFailureException.class)
    public MessageResult myErrorHandler(ObjectOptimisticLockingFailureException ex) {
        log.info("数据过期异常={}",ex);
        MessageResult result = MessageResult.error(6000, msService.getMessage("DATA.EXPIRED"));
        return result;
    }

    /**
     * 拦截参数异常
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public MessageResult myErrorHandler(IllegalArgumentException e) {
        log.info("拦截参数异常={}",e);
        MessageResult result = MessageResult.error(e.getMessage());
        return result;
    }

    /**
     * 拦截绑定参数异常
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public MessageResult myErrorHandler(ServletRequestBindingException e) {
        log.info("拦截绑定参数异常={}",e);
        MessageResult result = MessageResult.error(3000, msService.getMessage("PARAMETER.BINDING.ERROR"));
        return result;
    }

    /**
     * 拦截数据过期异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = InformationExpiredException.class)
    public MessageResult myErrorHandler(InformationExpiredException ex) {
        log.info("拦截数据过期异常={}",ex);
        MessageResult result = MessageResult.error(msService.getMessage("DATA.EXPIRED"));
        return result;
    }

    /**
     * 拦截极验证验证失败异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = GeeTestException.class)
    public MessageResult myErrorHandler(GeeTestException ex) {
        log.info("拦截极验证验证失败异常={}",ex);
        MessageResult result = MessageResult.error(ex.getMessage());
        return result;
    }

    /**
     * 拦截Spring Validation异常
     * @param exception
     * @return
     */
    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    public MessageResult validationExceptionHandler(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
        return MessageResult.error(500, message);
    }
    @ResponseBody
    @ExceptionHandler(BindException.class)
    public MessageResult bindExceptionHandler(final BindException e) {
        String message = e.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining());
        return MessageResult.error(500, message);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public MessageResult methodArgumentNotValidExceptionHandler(final MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining());
        return MessageResult.error(500, message);
    }

    /**
     * 拦截异常
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public MessageResult myErrorHandler(Exception ex) {
        log.info("拦截异常={}",ex);
        MessageResult result = MessageResult.error(msService.getMessage("UNKNOWN.ERROR"));
        if(StringUtils.isNotBlank(ex.getMessage())){
            result = MessageResult.error(ex.getMessage());
        }

        return result;
    }

    /**
     * @param
     * @return
     * @author MrGao
     * @description 错误请求方式异常  HttpRequestMethodNotSupportedException
     * @date 2018/2/28 17:32
     */
    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public MessageResult httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.info("错误请求方式异常={}",ex);
        String methods = "";
        //支持的请求方式
        String[] supportedMethods = ex.getSupportedMethods();
        for (String method : supportedMethods) {
            methods += method;
        }
        MessageResult result = MessageResult.error("Request method " + ex.getMethod() + "  not supported !" +
                " supported method : " + methods + "!");
        return result;
    }
}
