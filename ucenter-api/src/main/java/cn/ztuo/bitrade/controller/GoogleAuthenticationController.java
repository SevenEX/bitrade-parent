package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.SecurityVerification;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.util.GoogleAuthenticatorUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * @author shenzucai
 * @time 2018.04.09 11:07
 */
@RestController
@Slf4j
@RequestMapping("/google")
@Api(tags = "谷歌验证")
public class GoogleAuthenticationController extends BaseController{

    @Autowired
    private MemberService memberService;
    @Value("${google.auth.url}")
    private String googleAuthUrl;

    @Autowired
    private LocaleMessageSourceService messageSource;
    /**
     * 验证google
     * @author shenzucai
     * @time 2018.04.09 11:36
     * @param user
     * @param codes
     * @return true
     */

    @RequestMapping(value = "/yzgoogle",method = RequestMethod.GET)
    public MessageResult yzgoogle(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String codes) {
        // enter the code shown on device. Edit this and run it fast before the
        // code expires!
        long code = Long.parseLong(codes);
        Member member = memberService.findOne(user.getId());
        long t = System.currentTimeMillis();
        GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
        //  ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        boolean r = ga.check_code(member.getGoogleKey(), code, t);
        log.info("rrrr="+r);
        if(!r){
            return MessageResult.error(msService.getMessage("GOOGLE_FAIL"));
        }
        else{
            return MessageResult.success(msService.getMessage("GOOGLE_SUCCESS"));
        }
    }


    /**
     * 生成谷歌认证码
     * @return
     */
    @RequestMapping(value = "/sendgoogle",method = RequestMethod.GET)
    public MessageResult  sendgoogle(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        /*for(int i = 0;i<50;i++){
            log.info("######################       开始循环次数={}    ######################",i+1);
            GoogleAuthenticatorUtil.generateSecretKey();
            log.info("######################       结束循环次数={}    ######################",i+1);
        }*/
        log.info("开始进入用户id={}",user.getId());
        long current = System.currentTimeMillis();
        Member member = memberService.findOne(user.getId());
        log.info("查询完毕 耗时={}",System.currentTimeMillis()-current);
        if (member == null){
            return  MessageResult.error(msService.getMessage("RE_LOGIN"));
        }
        String secret = GoogleAuthenticatorUtil.generateSecretKey();
        log.info("secret完毕 耗时={}",System.currentTimeMillis()-current);
        String qrBarcodeURL = GoogleAuthenticatorUtil.getQRBarcodeURL(member.getMobilePhone(),
                googleAuthUrl, secret);
        log.info("qrBarcodeURL完毕 耗时={}",System.currentTimeMillis()-current);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("link",qrBarcodeURL);
        jsonObject.put("secret",secret);
        log.info("jsonObject完毕 耗时={}",System.currentTimeMillis()-current);
        MessageResult messageResult = new MessageResult();
        messageResult.setData(jsonObject);
        messageResult.setMessage(messageSource.getMessage("SUCCESS"));
        log.info("执行完毕 耗时={}",System.currentTimeMillis()-current);
        return  messageResult;

    }


    /**
     * google解绑
     * @author shenzucai
     * @time 2018.04.09 12:47
     * @param user
     * @return true
     */

    @RequestMapping(value = "/jcgoogle" ,method = RequestMethod.POST)
    @SecurityVerification(SysConstant.TOKEN_RESET_GOOGLE_AUTH)
    public MessageResult jcgoogle(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        member.setGoogleDate(new Date());
        member.setGoogleState(0);
        Member result = memberService.save(member);
        if(result != null){
            return MessageResult.success(msService.getMessage("GOOGLE_UNBIND_SUCCESS"));
        }else{
            return MessageResult.error(msService.getMessage("GOOGLE_UNBIND_FAIL"));
        }
    }




        //ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
        /**
         * 绑定google
         * @author shenzucai
         * @time 2018.04.09 15:19
         * @param codes
         * @param user
         * @return true
         */
        @RequestMapping(value = "/googleAuth" ,method = RequestMethod.POST)
        public MessageResult googleAuth(String codes, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,String secret) {

            Member member = memberService.findOne(user.getId());
            long code = Long.parseLong(codes);
            long t = System.currentTimeMillis();
            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            boolean r = ga.check_code(secret, code, t);
            if(!r){
                return MessageResult.error(msService.getMessage("GOOGLE_FAIL"));
            }else{
                member.setGoogleState(1);
                member.setGoogleKey(secret);
                member.setGoogleDate(new Date());
                Member result = memberService.save(member);
                if(result != null){
                    return MessageResult.success(msService.getMessage("GOOGLE_BIND_SUCCESS"));
                }else{
                    return MessageResult.error(msService.getMessage("GOOGLE_BIND_FAIL"));
                }
            }
        }

}
