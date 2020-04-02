package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.core.Encrypt;
import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.service.AdminService;
import cn.ztuo.bitrade.util.GoogleAuthenticatorUtil;
import cn.ztuo.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
/**
 * @author shenzucai
 * @time 2018.04.09 11:07
 */
@RestController
@Slf4j
@RequestMapping("/google")
public class GoogleVerificationController {
    @Autowired
    private AdminService adminService;
    @Value("${bdtop.system.md5.key}")
    private String md5Key;
    @Value("${google.auth.url}")
    private String googleAuthUrl;

    /**
     * 当前账户是否绑定了谷歌验证，1是0否
     * @param admin
     * @return
     */
    @RequiresPermissions("google:auth:googleState")
    @GetMapping("googleState")
    public MessageResult adminDetail(@SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin){
        MessageResult result=MessageResult.success();
        if(admin.getGoogleState()==null){
            admin.setGoogleState(0);
        }
        result.setData(admin.getGoogleState());
        return result;
    }

    /**
     * 验证google
     * @author shenzucai
     * @time 2018.04.09 11:36
     * @param userName
     * @param codes
     * @return true
     */

    @RequestMapping(value = "/yzgoogle",method = RequestMethod.POST)
    public MessageResult yzgoogle(String userName,String password, String codes) {
        // enter the code shown on device. Edit this and run it fast before the
        // code expires!
        password = Encrypt.MD5(password + md5Key);
        Admin admin=adminService.login(userName,password);
        if(admin==null){
            return MessageResult.error("用户名或密码错误");
        }
        long code = Long.parseLong(codes);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        //  ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        boolean r = ga.check_code(admin.getGoogleKey(), code, t);
        log.info("rrrr="+r);
        if(!r){
            return MessageResult.error("验证失败");
        }
        else{
            return MessageResult.success("验证通过");
        }
    }


    /**
     * 生成谷歌认证码
     * @return
     */
    @RequiresPermissions("google:auth:sendgoogle")
    @RequestMapping(value = "/sendgoogle",method = RequestMethod.GET)
    public MessageResult  sendgoogle(@SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin) {
        /*for(int i = 0;i<50;i++){
            log.info("######################       开始循环次数={}    ######################",i+1);
            GoogleAuthenticatorUtil.generateSecretKey();
            log.info("######################       结束循环次数={}    ######################",i+1);
        }*/
        log.info("开始进入用户id={}",admin.getId());
        long current = System.currentTimeMillis();
        log.info("查询完毕 耗时={}",System.currentTimeMillis()-current);
        if (admin == null){
            return  MessageResult.error("未登录");
        }
        String secret = GoogleAuthenticatorUtil.generateSecretKey();
        log.info("secret完毕 耗时={}",System.currentTimeMillis()-current);
        String qrBarcodeURL = GoogleAuthenticatorUtil.getQRBarcodeURL(admin.getId().toString(),
                googleAuthUrl, secret);
        log.info("qrBarcodeURL完毕 耗时={}",System.currentTimeMillis()-current);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("link",qrBarcodeURL);
        jsonObject.put("secret",secret);
        log.info("jsonObject完毕 耗时={}",System.currentTimeMillis()-current);
        MessageResult messageResult = new MessageResult();
        messageResult.setData(jsonObject);
        messageResult.setMessage("获取成功");
        log.info("执行完毕 耗时={}",System.currentTimeMillis()-current);
        return  messageResult;

    }


    /**
     * google解绑
     * @author shenzucai
     * @time 2018.04.09 12:47
     * @param codes
     * @param admin
     * @return true
     */

    @RequiresPermissions("google:auth:reset")
    @RequestMapping(value = "/jcgoogle" ,method = RequestMethod.POST)
    public MessageResult jcgoogle(String codes,
                                  @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,
                                  String password) {
        // enter the code shown on device. Edit this and run it fast before the
        // code expires!
        //String GoogleKey = (String) request.getSession().getAttribute("googleKey");
        String GoogleKey = admin.getGoogleKey();
        if(StringUtils.isEmpty(password)){
            return MessageResult.error("密码不能为空");
        }
        try {
            if(!(Encrypt.MD5(password + md5Key).equals(admin.getPassword().toLowerCase()))){
                return MessageResult.error("密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long code = Long.parseLong(codes);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        // ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        boolean r = ga.check_code(GoogleKey, code, t);
        if(!r){
            return MessageResult.error("验证失败");

        }else{
            admin.setGoogleDate(new Date());
            admin.setGoogleState(0);
            Admin result=adminService.save(admin);
            if(result != null){
                return MessageResult.success("解绑成功");
            }else{
                return MessageResult.error("解绑失败");
            }
        }
    }




    //ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
    /**
     * 绑定google
     * @author shenzucai
     * @time 2018.04.09 15:19
     * @param codes
     * @param admin
     * @return true
     */
    @RequiresPermissions("google:auth:bind")
    @RequestMapping(value = "/googleAuth" ,method = RequestMethod.POST)
    public MessageResult googleAuth(String codes, @SessionAttribute(SysConstant.SESSION_ADMIN)Admin admin,String secret) {
        long code = Long.parseLong(codes);
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        boolean r = ga.check_code(secret, code, t);
        if(!r){
            return MessageResult.error("验证失败");
        }else{
            admin.setGoogleState(1);
            admin.setGoogleKey(secret);
            admin.setGoogleDate(new Date());
            Admin result = adminService.save(admin);
            if(result != null){
                return MessageResult.success("绑定成功");
            }else{
                return MessageResult.error("绑定失败");
            }
        }
    }
}
