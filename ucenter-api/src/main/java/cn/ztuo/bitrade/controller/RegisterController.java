package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.annotation.SecurityVerification;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.MemberLevelEnum;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.enums.VerificationType;
import cn.ztuo.bitrade.event.MemberEvent;
import cn.ztuo.bitrade.service.CountryService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;

import cn.ztuo.bitrade.util.*;
import cn.ztuo.bitrade.utils.TengXunWatherProofUtil;
import cn.ztuo.bitrade.utils.ValidateUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static cn.ztuo.bitrade.constant.SysConstant.*;
import static cn.ztuo.bitrade.util.MessageResult.error;
import static cn.ztuo.bitrade.util.MessageResult.success;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * 会员注册
 *
 * @author Seven
 * @date 2019年12月29日
 */
@Controller
@Slf4j
@Api(tags = "用户注册")
public class RegisterController extends BaseController{

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${bdtop.system.host}")
    private String host;
    @Value("${bdtop.system.name}")
    private String company;
    @Value("${sms.isTset}")
    private Boolean isTset;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MemberService memberService;

    @Autowired
    private IdWorkByTwitter idWorkByTwitter;

    @Autowired
    private MemberEvent memberEvent;

    @Autowired
    private CountryService countryService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    private String userNameFormat = "U%06d";

    /**
     * 注册支持的国家
     *
     * @return
     */
    @RequestMapping(value = "/support/country", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "注册支持的国家")
    @MultiDataSource(name = "second")
    public MessageResult allCountry() {
        MessageResult result = success();
        List<Country> list = countryService.getAllCountry();
        result.setData(list);
        return result;
    }

    /**
     * 检查用户名是否重复
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "/register/check/username",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "检查用户名是否重复（暂弃）")
    @MultiDataSource(name = "second")
    public MessageResult checkUsername(String username) {
        MessageResult result = success();
        if (memberService.usernameIsExist(username)) {
            result.setCode(500);
            result.setMessage(localeMessageSourceService.getMessage("ACTIVATION_FAILS_USERNAME"));
        }
        return result;
    }

    /**
     * 检查手机/邮箱是否重复
     *
     * @param phone
     * @return
     */
    @RequestMapping(value = "/register/check/phone",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "检查手机/邮箱是否重复")
    @MultiDataSource(name = "second")
    public MessageResult checkPhone(String phone) {
        MessageResult result = success();
        if (memberService.phoneIsExist(phone)) {
            result.setCode(500);
            result.setMessage(localeMessageSourceService.getMessage("PHONE_EXIST"));
        }
        if (memberService.emailIsExist(phone)) {
            result.setCode(500);
            result.setMessage(localeMessageSourceService.getMessage("EMAIL_EXIST"));
        }
        return result;
    }

    /**
     * 检查手机/邮箱是否重复
     * @return
     */
    @RequestMapping(value = "/register/check/promotion",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    @ApiOperation(value = "检查邀请码是否存在")
    @MultiDataSource(name = "second")
    public MessageResult checkPromotion(String promotion) {
        Member parentMember = memberService.findMemberByPromotionCode(promotion);
        if (!StringUtils.isEmpty(promotion) && parentMember == null) {
            return error(localeMessageSourceService.getMessage("INVITATION_CODE_NOT_EXISTS"));
        }
        return success();
    }


    /**
     * 邮箱注册
     *
     * @param loginByEmail
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/register/email",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    @Transactional
    @ApiOperation(value = "邮箱注册")
    @SecurityVerification(value = TOKEN_REGISTER_MAIL, accountKey = "email", isRegister = true, verificationType = VerificationType.Email)
    public MessageResult registerByEmail(@Valid LoginByEmail loginByEmail, HttpServletRequest request,BindingResult
            bindingResult) throws Exception{
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        String ip = getRemoteIp(request);
        //防水验证
//        boolean resultProof = TengXunWatherProofUtil.watherProof(loginByEmail.getTicket(), loginByEmail.getRandStr(), ip);
//        if (!resultProof) {
//            return error("验证码错误");
//        }
        if(!StringUtils.isEmpty(loginByEmail.getPromotion())) {
            Member parentMember = memberService.findMemberByPromotionCode(loginByEmail.getPromotion());
            if (parentMember == null) {
                return error(localeMessageSourceService.getMessage("INVITATION_CODE_NOT_EXISTS"));
            }
        }
//        Member parentMember = memberService.findMemberByPromotionCode(loginByEmail.getPromotion());
//        if (!StringUtils.isEmpty(loginByEmail.getPromotion()) && parentMember == null) {
//            return error(localeMessageSourceService.getMessage("INVITATION_CODE_NOT_EXISTS"));
//        }
        //不可重复随机数
        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex();
        //生成密码
        String password = Md5.md5Digest(loginByEmail.getPassword() + credentialsSalt).toLowerCase();
        Member member = new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        Location location = new Location();
        location.setCountry(loginByEmail.getCountry());
        member.setLocation(location);
        Country country = new Country();
        country.setZhName(loginByEmail.getCountry());
        member.setCountry(country);
        member.setPassword(password);
        member.setEmail(loginByEmail.getEmail());
        member.setEmailState(1);
        member.setSalt(credentialsSalt);
        member.setKycStatus(0);
        Member member1 = memberService.save(member);
        if (member1 != null) {
            member1.setPromotionCode(String.format(userNameFormat, member1.getId()) + GeneratorUtil.getNonceString(2));
            memberEvent.onRegisterSuccess(member1, loginByEmail.getPromotion());
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_SUCCESSFUL"));
        } else {
            request.setAttribute("result", localeMessageSourceService.getMessage("ACTIVATION_FAILS"));
        }
        return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
    }

    /**
     * 手机注册
     *
     * @param loginByPhone
     * @param bindingResult
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/register/phone",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "手机注册")
    @SecurityVerification(value = TOKEN_REGISTER_PHONE, accountKey = "phone", isRegister = true, verificationType = VerificationType.Phone)
    public MessageResult loginByPhone(
            @Valid LoginByPhone loginByPhone,
            BindingResult bindingResult,HttpServletRequest request) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
       // String ip = getRemoteIp(request);
        //防水验证
//        boolean resultProof = TengXunWatherProofUtil.watherProof(loginByPhone.getTicket(), loginByPhone.getRandStr(), ip);
//        if (!resultProof) {
//            return error("验证码验证失败,请重新获取验证码");
//        }
        if(!StringUtils.isEmpty(loginByPhone.getPromotion())) {
            Member parentMember = memberService.findMemberByPromotionCode(loginByPhone.getPromotion());
            if (parentMember == null) {
                return error(localeMessageSourceService.getMessage("INVITATION_CODE_NOT_EXISTS"));
            }
        }
        String phone = loginByPhone.getPhone();
        //不可重复随机数
        String loginNo = String.valueOf(idWorkByTwitter.nextId());
        //盐
        String credentialsSalt = ByteSource.Util.bytes(loginNo).toHex();
        //生成密码
        String password = Md5.md5Digest(loginByPhone.getPassword() + credentialsSalt).toLowerCase();
        Member member = new Member();
        member.setMemberLevel(MemberLevelEnum.GENERAL);
        Location location = new Location();
        location.setCountry(loginByPhone.getCountry());
        Country country = new Country();
        country.setZhName(loginByPhone.getCountry());
        member.setCountry(country);
        member.setLocation(location);
        member.setPassword(password);
        member.setMobilePhone(phone);
        member.setSalt(credentialsSalt);
        member.setPhoneState(1);
        member.setKycStatus(0);
        Member member1 = memberService.save(member);
        if (member1 != null) {
            member1.setPromotionCode(String.format(userNameFormat, member1.getId()) + GeneratorUtil.getNonceString(2));
            memberEvent.onRegisterSuccess(member1, loginByPhone.getPromotion());
            return success(localeMessageSourceService.getMessage("REGISTRATION_SUCCESS"));
        } else {
            return error(localeMessageSourceService.getMessage("REGISTRATION_FAILED"));
        }
    }

    /**
     * 发送绑定邮箱验证码
     *
     * @param email
     * @param user
     * @return
     */
    @RequestMapping(value = "/bind/email/code",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("获取绑定邮箱验证码")
    public MessageResult sendBindEmail(String email, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Assert.isTrue(ValidateUtil.isEmail(email), localeMessageSourceService.getMessage("WRONG_EMAIL"));
        Member member = memberService.findOne(user.getId());
        Assert.isNull(member.getEmail(), localeMessageSourceService.getMessage("BIND_EMAIL_REPEAT"));
        Assert.isTrue(!memberService.emailIsExist(email), localeMessageSourceService.getMessage("EMAIL_ALREADY_BOUND"));
        if(isTset) {
            redisUtil.set(EMAIL_BIND_CODE_PREFIX + email, "123456", 30, TimeUnit.MINUTES);
            return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
        }
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        if (redisUtil.get(EMAIL_BIND_CODE_PREFIX + email) != null) {
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND"));
        }
        try {
            sentEmailCode( email, code);
        } catch (Exception e) {
            e.printStackTrace();
            return error(localeMessageSourceService.getMessage("SEND_FAILED"));
        }
        return success(localeMessageSourceService.getMessage("SENT_SUCCESS_TEN"));
    }

    @Async
    public void sentEmailCode(String email, String code) throws MessagingException, IOException, TemplateException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company);
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("bindCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        helper.setText(html, true);

        //发送邮件
        javaMailSender.send(mimeMessage);
        log.info("send email for {},content:{}", email, html);
        redisUtil.set(EMAIL_BIND_CODE_PREFIX + email, code, 30, TimeUnit.MINUTES);
    }

    @Async
    public void sentResetPassword(String email, String code,String prefix) throws MessagingException, IOException, TemplateException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company);
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("resetPasswordCodeEmail.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        helper.setText(html, true);
        //发送邮件
        javaMailSender.send(mimeMessage);
        redisUtil.set(prefix + email, code, 10, TimeUnit.MINUTES);
    }

    /**
     * 忘记密码后重置密码
     *
     * @param password 新密码
     * @return
     */
    @RequestMapping(value = "/reset/login/password", method = RequestMethod.POST)
    @ApiOperation("忘记密码后重置密码")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(value = SysConstant.TOKEN_RESET_PASSWORD)
    public MessageResult forgetPassword(String account, String password) throws Exception {
        isTrue(ValidateUtils.validatePassword(password), msService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.findByPhoneOrEmail(account);
        isTrue(password.length() >= 6 && password.length() <= 20, localeMessageSourceService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        notNull(member, localeMessageSourceService.getMessage("MEMBER_NOT_EXISTS"));
        //生成密码
        String newPassword = Md5.md5Digest(password + member.getSalt()).toLowerCase();
        member.setPassword(newPassword);
        member.setLoginLock(BooleanEnum.IS_FALSE);
        //修改/重置登录密码 24小时不允许提币
        redisUtil.set(SysConstant.WITHDRAW_LOCK+member.getId(),true,SysConstant.WITHDRAW_LOCK_EXPIRE_TIME,TimeUnit.HOURS);
        return success();
    }

    /**
     * 发送新邮箱验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/update/email/code")
    @ApiOperation("发送新邮箱验证码")
    @ResponseBody
    public MessageResult updateEmailCode(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,String email){
        if(memberService.emailIsExist(email)){
            return MessageResult.error(msService.getMessage("REPEAT_EMAIL_REQUEST"));
        }
        Member member = memberService.findOne(user.getId());
        isTrue(member.getEmail()!=null,msService.getMessage("NOT_BIND_EMAIL"));
        Object cache = redisUtil.get(SysConstant.EMAIL_UPDATE_CODE_PREFIX + email);
        if(cache!=null){
            return error(localeMessageSourceService.getMessage("EMAIL_ALREADY_SEND"));
        }
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        try {
            sentResetPassword( email, code,EMAIL_UPDATE_CODE_PREFIX);
        } catch (MessagingException | IOException | TemplateException e) {
            e.printStackTrace();
        }
        return success();
    }


}
