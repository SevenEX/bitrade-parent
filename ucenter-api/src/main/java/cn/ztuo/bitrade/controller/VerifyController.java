package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.SecurityVerification;
import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.constant.VerifyType;
import cn.ztuo.bitrade.entity.Country;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.CountryService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.service.MemberVerifyRecordService;
import cn.ztuo.bitrade.service.VerifyCodeService;
import cn.ztuo.bitrade.util.*;
import com.google.common.base.Verify;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

import static cn.ztuo.bitrade.constant.SysConstant.*;
import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("/verify")
@Slf4j
@Api(tags = "二次验证")
public class VerifyController extends BaseController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberVerifyRecordService memberVerifyRecordService;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping(value = "/regSmsCode",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取注册短信验证码")
    public MessageResult regSmsCode(String phone, String country, String type) throws Exception {
        if(StringUtils.equals(type, TOKEN_REGISTER_PHONE)) {
            Assert.isTrue(!memberService.phoneIsExist(phone), () -> msService.getMessage("PHONE_ALREADY_EXISTS"));
        }
        else if(StringUtils.equals(type, TOKEN_RESET_PASSWORD)) {
            Member member = memberService.findByPhoneOrEmail(phone);
            Assert.notNull(member, msService.getMessage("USER_NOT_EXIST"));
            if (sendMemberSmsCode(type, member))
                return success(msService.getMessage("SENT_SUCCESS_TEN"));
            return error("illegal type");
        }
        else {
            return error("illegal type");
        }
        Assert.isTrue(!StringUtils.isEmpty(country), () -> msService.getMessage("REQUEST_ILLEGAL"));
        Country country1 = countryService.findOne(country);
        Assert.notNull(country1, () -> msService.getMessage("REQUEST_ILLEGAL"));
        verifyCodeService.sendSMSCode(phone, country1.getAreaCode(), type);
        return success(msService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping(value = "/regEmailCode",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取注册邮箱验证码")
    public MessageResult regEmailCode(String email, String type) throws Exception {
        if(StringUtils.equals(type, TOKEN_REGISTER_MAIL)) {
            Assert.isTrue(!memberService.emailIsExist(email), () -> msService.getMessage("EMAIL_ALREADY_BOUND"));
        }
        else if(StringUtils.equals(type, TOKEN_RESET_PASSWORD)) {
            Member member = memberService.findByPhoneOrEmail(email);
            Assert.notNull(member, msService.getMessage("USER_NOT_EXIST"));
            if (sendMemberEmailCode(type, member))
                return success(msService.getMessage("SENT_SUCCESS_TEN"));
            return error("illegal type");
        }
        else{
            return error("illegal type");
        }
        verifyCodeService.sentEmailCode(email, type);
        return success(msService.getMessage("SENT_SUCCESS_TEN"));
    }

    @RequestMapping(value = "/smsCode",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取短信验证码")
    public MessageResult smsCode(String type,@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        if (sendMemberSmsCode(type, member))
            return success(msService.getMessage("SENT_SUCCESS_TEN"));
        return error("illegal state");
    }

    private boolean sendMemberSmsCode(String type, Member member) throws Exception {
        if(member.getPhoneState() != 0){
            verifyCodeService.sendSMSCode(member.getMobilePhone(), member.getCountry().getAreaCode(), type);
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/emailCode",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取邮箱验证码")
    public MessageResult emailCode(String type,@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        if (sendMemberEmailCode(type, member))
            return success(msService.getMessage("SENT_SUCCESS_TEN"));
        return error("illegal state");
    }

    private boolean sendMemberEmailCode(String type, Member member) throws Exception {
        if(member.getEmailState() != 0){
            verifyCodeService.sentEmailCode(member.getEmail(), type);
            return true;
        }
        return false;
    }

    @RequestMapping(value = "/regCheck",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "注册验证验证码获取Token")
    public MessageResult regCheck(String type, String account, String emailCode, String phoneCode, String googleCode) {
        if(StringUtils.equals(type, TOKEN_REGISTER_PHONE)){
            verifyCodeService.checkSMS(account, type, phoneCode);
            verifyCodeService.removeSMSCode(account, type);
        }
        else if(StringUtils.equals(type, TOKEN_REGISTER_MAIL)){
            verifyCodeService.checkEmail(account, type, emailCode);
            verifyCodeService.removeEmailCode(account, type);
        }
        else if(StringUtils.equals(type, TOKEN_RESET_PASSWORD)){
            Member member = memberService.findByPhoneOrEmail(account);
            Assert.notNull(member, msService.getMessage("USER_NOT_EXIST"));
            return checkMemberCode(type, emailCode, phoneCode, googleCode, member);
        }
        else {
            return error("illegal request");
        }
        RMapCache<String, String> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.SECURITY_VERIFY + type);
        String token = UUIDUtil.getUUID();
        mapCache.put(account, token, 600, TimeUnit.SECONDS);
        return success(token);
    }

    @RequestMapping(value = "/check",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "验证验证码获取Token")
    public MessageResult check(String type, String emailCode, String phoneCode, String googleCode, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        return checkMemberCode(type, emailCode, phoneCode, googleCode, member);
    }

    private MessageResult checkMemberCode(String type, String emailCode, String phoneCode, String googleCode, Member member) {
        if(member.getPhoneState() != 0){
            verifyCodeService.checkSMS(member.getMobilePhone(), type, phoneCode);
        }
        if(member.getEmailState() != 0){
            verifyCodeService.checkEmail(member.getEmail(), type, emailCode);
        }
        if(member.getGoogleState() != 0){
            notNull(googleCode, msService.getMessage("MISSING_VERIFICATION_CODE"));
            if(member.getGoogleState()!=0) {
                boolean r = GoogleAuthenticatorUtil.checkCodes(googleCode,member.getGoogleKey());
                if (!r) {
                    return MessageResult.error(msService.getMessage("GOOGLE_AUTH_FAILD"));
                }
            }else{
                return MessageResult.error(msService.getMessage("BIND_GOOGLE_FIRST"));
            }
        }
        if(member.getPhoneState() != 0){
            verifyCodeService.removeSMSCode(member.getMobilePhone(), type);
        }
        if(member.getEmailState() != 0){
            verifyCodeService.removeEmailCode(member.getEmail(), type);
        }
        RMapCache<String, String> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.SECURITY_VERIFY + type);
        String token = UUIDUtil.getUUID();
        mapCache.put(String.valueOf(member.getId()), token, 600, TimeUnit.SECONDS);
        return success(token);
    }

    @RequestMapping(value = "/switch",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "关闭/开启二次验证")
    @SecurityVerification(SysConstant.TOKEN_SWITCH_VERIFY)
    public MessageResult switchState(HttpServletRequest request, Integer emailState, Integer phoneState, Integer googleState, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        String host = RequestUtil.remoteIp(request);
        VerifyType type = null;
        if(emailState != 0 && StringUtils.isNotEmpty(member.getEmail())){
            emailState = 1;
        }
        if(phoneState != 0 && StringUtils.isNotEmpty(member.getMobilePhone())){
            phoneState = 1;
        }
        if(googleState != 0 && StringUtils.isNotEmpty(member.getGoogleKey())){
            googleState = 1;
        }
        if(!emailState.equals(member.getEmailState())) {
            type = emailState ==1 ? VerifyType.ON_EMAIL : VerifyType.OFF_EMAIL;
            memberVerifyRecordService.saveVerifyRecord(member, type, host);
            //关闭安全验证 24小时不允许提币
            if(emailState == 0)
                redisUtil.set(SysConstant.WITHDRAW_LOCK+member.getId(),true,SysConstant.WITHDRAW_LOCK_EXPIRE_TIME,TimeUnit.HOURS);
        }
        if(!phoneState.equals(member.getPhoneState())) {
            type = phoneState ==1 ? VerifyType.ON_SMS : VerifyType.OFF_SMS;
            memberVerifyRecordService.saveVerifyRecord(member, type, host);
            if(phoneState == 0)
                redisUtil.set(SysConstant.WITHDRAW_LOCK+member.getId(),true,SysConstant.WITHDRAW_LOCK_EXPIRE_TIME,TimeUnit.HOURS);
        }
        if(!googleState.equals(member.getGoogleState())) {
            type = googleState ==1 ? VerifyType.ON_GOOGLE : VerifyType.OFF_GOOGLE;
            memberVerifyRecordService.saveVerifyRecord(member, type, host);
            if(googleState == 0)
                redisUtil.set(SysConstant.WITHDRAW_LOCK+member.getId(),true,SysConstant.WITHDRAW_LOCK_EXPIRE_TIME,TimeUnit.HOURS);
        }
        Assert.isTrue(emailState != 0 || phoneState != 0 || googleState != 0, msService.getMessage("NO_AUTH_ERROR"));
        member.setEmailState(emailState != 0 ? 1 : 0);
        member.setPhoneState(phoneState != 0 ? 1 : 0);
        member.setGoogleState(googleState != 0 ? 1 : 0);
        memberService.saveAndFlush(member);
        return success(member);
    }
}
