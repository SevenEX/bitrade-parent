package cn.ztuo.bitrade.controller.common;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * @author Seven
 * @date 2019年12月19日
 */
@Component
public class BaseAdminController extends BaseController {

    @Autowired
    private RedisUtil redisUtil;

    protected Admin getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Admin) session.getAttribute(SysConstant.SESSION_ADMIN);
    }

    /**
     * 判断手机验证码正确否
     * @param code 验证码
     * @param key redis中的key 前缀+手机号
     * @return
     */
    protected MessageResult checkCode(String code, String key){
        Object value = redisUtil.get(key);
        if(value==null)
            return error(msService.getMessage("CODE_NOT_EXIST_RESEND"));
        if(!value.toString().equals(code))
            return  error(msService.getMessage("CODE_ERROR"));
        redisUtil.delete(key);
        /**
         * 十分钟之内无需再次验证
         */
        redisUtil.set(key+"_PASS",true,10, TimeUnit.MINUTES);
        return success(msService.getMessage("CODE_CORRECT"));
    }
}
