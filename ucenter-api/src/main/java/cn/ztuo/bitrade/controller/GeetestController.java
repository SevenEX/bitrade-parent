package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.system.GeetestLib;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author Seven
 * @date 2019年02月23日
 */
@RestController
@Slf4j
@Api(tags = "极验")
public class GeetestController extends BaseController {

    @Autowired
    private GeetestLib gtSdk;

    @RequestMapping(value = "/start/captcha")
    public String startCaptcha(HttpServletRequest request) {
        String resStr = "{}";
        String userid = "bdtop";
        //自定义参数,可选择添加
        HashMap<String, String> param = new HashMap<String, String>();
        String ip = getRemoteIp(request);
        param.put("user_id", userid); //网站用户id
        param.put("client_type", "web"); //web:电脑上的浏览器；h5:手机上的浏览器，包括移动应用内完全内置的web_view；native：通过原生SDK植入APP应用的方式
        param.put("ip_address", ip); //传输用户请求验证时所携带的IP
        //进行验证预处理
        int gtServerStatus = gtSdk.preProcess(param);
        //将服务器状态设置到session中
        request.getSession().setAttribute(gtSdk.gtServerStatusSessionKey, gtServerStatus);
        //将userid设置到session中
        request.getSession().setAttribute("userid", userid);
        resStr = gtSdk.getResponseStr();
        return resStr;
    }
}
