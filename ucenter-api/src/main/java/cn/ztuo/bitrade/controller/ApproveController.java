package cn.ztuo.bitrade.controller;


import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.annotation.SecurityVerification;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.enums.CredentialsType;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.*;
import cn.ztuo.bitrade.utils.ValidateUtils;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.ztuo.bitrade.constant.BooleanEnum.IS_FALSE;
import static cn.ztuo.bitrade.constant.BooleanEnum.IS_TRUE;
import static cn.ztuo.bitrade.constant.CertifiedBusinessStatus.*;
import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;
import static org.springframework.util.Assert.*;


/**
 * 用户中心认证
 *
 * @author Seven
 * @date 2019年01月09日
 */
@RestController
@RequestMapping("/approve")
@Slf4j
@Api(tags = "用户中心")
public class ApproveController extends BaseController{

    private static Logger logger = LoggerFactory.getLogger(ApproveController.class);

    @Autowired
    private MemberService memberService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private BusinessAuthDepositService businessAuthDepositService;
    @Autowired
    private BusinessCancelApplyService businessCancelApplyService;
    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;
    @Autowired
    private SeFeeChangeRecordService seFeeChangeRecordService;
    @Autowired
    private OrderService orderService ;
    @Autowired
    private AdvertiseService advertiseService ;

    @Autowired
    private OtcWalletService otcWalletService;
    @Value("${idCard.success.limit:0}")
    private int realTimes;
    @Value("${system.code.type:0}")
    private int codeType;
    @Autowired
    private LocaleMessageSourceService messageSource;
    /**
     * 设置或更改用户头像
     *
     * @param user
     * @param url
     * @return
     */
    @RequestMapping(value = "/change/avatar",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "设置或更改用户头像")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult update(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String url) {
        Member member = memberService.findOne(user.getId());
        member.setAvatar(url);
        return MessageResult.success();
    }

    /**
     * 安全设置
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/security/setting",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "安全设置")
    public MessageResult securitySetting(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {

        Member member = memberService.findOne(user.getId());
        String idNumber = member.getIdNumber();

        MemberSecurity memberSecurity = MemberSecurity.builder().username(member.getUsername())
                .createTime(member.getRegistrationTime())
                .id(member.getId())
                //.emailVerified(StringUtils.isEmpty(member.getEmail()) ? IS_FALSE : IS_TRUE)
                .emailVerified(member.getEmailState().equals(0) ? IS_FALSE : IS_TRUE)
                .email(MaskUtil.maskEmail(member.getEmail()))
                .mobilePhone(MaskUtil.maskMobile(member.getMobilePhone()))
                .fundsVerified(StringUtils.isEmpty(member.getJyPassword()) ? IS_FALSE : IS_TRUE)
                .loginVerified(IS_TRUE)
                //.phoneVerified(StringUtils.isEmpty(member.getMobilePhone()) ? IS_FALSE : IS_TRUE)
                .phoneVerified(member.getPhoneState().equals(0) ? IS_FALSE : IS_TRUE)
                .googleVerified(member.getGoogleState().equals(0) ? IS_FALSE : IS_TRUE)
                .realName(member.getRealName())
                .idCard(StringUtils.isEmpty(idNumber) ? null : idNumber.substring(0, 2) + "**********" + idNumber.substring(idNumber.length() - 2))
                .realVerified(StringUtils.isEmpty(member.getRealName()) ? IS_FALSE : IS_TRUE)
                .realAuditing(member.getRealNameStatus().equals(RealNameStatus.AUDITING) ? IS_TRUE : IS_FALSE)
                .avatar(member.getAvatar())
                .accountVerified((member.getBankInfo() == null && member.getAlipay() == null && member.getWechatPay() == null) ? IS_FALSE : IS_TRUE)
                .googleStatus(member.getGoogleKey()==null ? 0:1)
                .transactions(member.getTransactions())
                .transactionTime(member.getTransactionTime())
                .level(member.getMemberLevel().getOrdinal())
                .integration(member.getIntegration())
                .kycStatus(member.getKycStatus())
                .memberGradeId(member.getMemberGradeId())
                //.googleState(member.getGoogleState())
                .memberLevel(member.getMemberLevel())
                .isQuick(member.getIsQuick())
                .build();
        if (memberSecurity.getRealAuditing().equals(IS_FALSE) && memberSecurity.getRealVerified().equals(IS_FALSE)) {
            List<MemberApplication> memberApplication = memberApplicationService.findLatelyReject(member);
            memberSecurity.setRealNameRejectReason(memberApplication == null || memberApplication.size() == 0 ? null : memberApplication.get(0).getRejectReason());
        }else if (member.getKycStatus() == 3){
            List<Integer> kycStatus = Arrays.asList(3);
            MemberApplication memberApplication = memberApplicationService.findMemberApplicationByKycStatusIn
                    (kycStatus,member);
            if (memberApplication != null){
                memberSecurity.setRealNameRejectReason(memberApplication.getRejectReason());
            }
        }
        int count = 0;
        if(member.getEmailState() != 0){
            count++;
        }
        if(member.getPhoneState() != 0){
            count++;
        }
        if(member.getGoogleState() != 0){
            count++;
        }
        memberSecurity.setSecurityMessage(count < 2 ? msService.getMessage("SECURITY_ALERT_MESSAGE"): "");
        MessageResult result = MessageResult.success(messageSource.getMessage("SUCCESS"));
        result.setData(memberSecurity);
        return result;
    }

    /**
     * 设置资金密码
     *
     * @param jyPassword
     * @param user
     * @return
     */
    @RequestMapping(value = "/transaction/password",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "设置资金密码")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jyPassword", value = "交易密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "name", value = "商家昵称", required = true, dataType = "String"),
    })
    @SecurityVerification(SysConstant.TOKEN_RESET_TRANS_PASSWORD)
    public MessageResult approveTransaction(
            @RequestParam("jyPassword") String jyPassword,
            @RequestParam("name") String name,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        //校验密码
        isTrue(ValidateUtils.validatePassword(jyPassword), msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.findOne(user.getId());
        Assert.isNull(member.getJyPassword(), msService.getMessage("REPEAT_SETTING"));
        //生成密码
        String jyPass = Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase();
        member.setJyPassword(jyPass);
        member.setUsername(name);
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * 重置资金密码
     *
     * @param newPassword
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/reset/transaction/password",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "重置资金密码")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(SysConstant.TOKEN_RESET_TRANS_PASSWORD)
    public MessageResult resetTransaction(
            @RequestParam String newPassword,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        hasText(newPassword, msService.getMessage("MISSING_NEW_JY_PASSWORD"));
        isTrue(ValidateUtils.validatePassword(newPassword), msService.getMessage("JY_PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.findOne(user.getId());
        member.setJyPassword(Md5.md5Digest(newPassword + member.getSalt()).toLowerCase());
        return MessageResult.success(msService.getMessage("SETTING_JY_PASSWORD"));
    }

    /**
     * 绑定手机号
     *
     * @param phone
     * @param code
     * @param user
     * @return
     */
    @RequestMapping(value = "/bind/phone",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "绑定手机号")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(SysConstant.TOKEN_PHONE_BIND)
    public MessageResult bindPhone(String phone, String code, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        hasText(phone, msService.getMessage("MISSING_PHONE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        if ("中国".equals(user.getLocation().getCountry())) {
            if (!ValidateUtil.isMobilePhone(phone.trim())) {
                return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
            }
        }
        Object cache = redisUtil.get(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        Member member1 = memberService.findByPhone(phone);
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        if (!code.equals(cache.toString())) {
            return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        } else {
            redisUtil.delete(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
        }
        Member member = memberService.findOne(user.getId());
        isTrue(member.getMobilePhone() == null, msService.getMessage("REPEAT_PHONE_REQUEST"));
        member.setMobilePhone(phone);
        member.setPhoneState(1);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }


    /**
     * 更改登录密码
     *
     * @param request
     * @param oldPassword
     * @param newPassword
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/update/password",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "更改登录密码")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(SysConstant.TOKEN_RESET_PASSWORD)
    public MessageResult updateLoginPassword(
            HttpServletRequest request,
            String oldPassword,
            String newPassword,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        hasText(oldPassword, msService.getMessage("MISSING_OLD_PASSWORD"));
        hasText(newPassword, msService.getMessage("MISSING_NEW_PASSWORD"));
        isTrue(ValidateUtils.validatePassword(newPassword), msService.getMessage("PASSWORD_LENGTH_ILLEGAL"));
        Member member = memberService.findOne(user.getId());
        isTrue(Md5.md5Digest(oldPassword + member.getSalt()).toLowerCase().equals(member.getPassword()), msService.getMessage("PASSWORD_ERROR"));
        request.removeAttribute(SysConstant.SESSION_MEMBER);
        member.setPassword(Md5.md5Digest(newPassword + member.getSalt()).toLowerCase());
        //修改/重置登录密码 24小时不允许提币
        redisUtil.set(SysConstant.WITHDRAW_LOCK+member.getId(),true,SysConstant.WITHDRAW_LOCK_EXPIRE_TIME, TimeUnit.HOURS);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 绑定邮箱
     *
     * @param code
     * @param email
     * @param user
     * @return
     */
    @RequestMapping(value = "/bind/email",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "绑定邮箱")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(SysConstant.TOKEN_EMAIL_BIND)
    public MessageResult bindEmail(String code, String email, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user){
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        hasText(email, msService.getMessage("MISSING_EMAIL"));
        isTrue(ValidateUtil.isEmail(email), msService.getMessage("EMAIL_FORMAT_ERROR"));
        Object cache = redisUtil.get(SysConstant.EMAIL_BIND_CODE_PREFIX + email);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        isTrue(code.equals(cache.toString()), msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        Member member = memberService.findOne(user.getId());
        isTrue(member.getEmail() == null, msService.getMessage("REPEAT_EMAIL_REQUEST"));
        member.setEmail(email);
        member.setEmailState(1);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 换邮箱
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/update/email",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "换邮箱")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(SysConstant.TOKEN_EMAIL_UNTIE)
    public MessageResult updateEmail(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String newEmailCode,String newEmail){
        hasText(newEmailCode, msService.getMessage("MISSING_VERIFICATION_CODE"));
        hasText(newEmail, msService.getMessage("MISSING_EMAIL"));
        Member member = memberService.findOne(user.getId());
        isTrue(member.getEmail()!=null,msService.getMessage("NOT_BIND_EMAIL"));
        Object newEmailCache = redisUtil.get(SysConstant.EMAIL_UPDATE_CODE_PREFIX + newEmail);
        notNull(newEmailCache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        isTrue(newEmailCode.equals(newEmailCache.toString()), msService.getMessage("VERIFICATION_CODE_INCORRECT"));
        redisUtil.delete(SysConstant.EMAIL_UPDATE_CODE_PREFIX + newEmail);
        member.setEmail(newEmail);
        return MessageResult.success();
    }

    /**
     * 实名认证（kyc1）
     *
     * @param realName
     * @param idCard
     * @param user
     * @return
     */
    @RequestMapping(value = "/real/name",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "实名认证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "realName", value = "姓名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "idCard", value = "证件号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "idCardFront", value = "正面图片", required = true, dataType = "String"),
            @ApiImplicitParam(name = "idCardBack", value = "反面图片", required = true, dataType = "String"),
            @ApiImplicitParam(name = "handHeldIdCard", value = "手持证件照片", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "0、身份证；1、护照；2、驾照；3、其他（默认身份证）", required = false, dataType = "Integer"),
    })
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realApprove(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,String realName, String idCard, String idCardFront,
                                     String idCardBack, String handHeldIdCard,
                                     @RequestParam(name = "type",required = false,defaultValue = "0") Integer authType) {
        hasText(realName, msService.getMessage("MISSING_REAL_NAME"));
        hasText(idCard, msService.getMessage("MISSING_ID_CARD"));
        hasText(idCardFront, msService.getMessage("MISSING_ID_CARD_FRONT"));
        hasText(idCardBack, msService.getMessage("MISSING_ID_CARD_BACK"));
        hasText(handHeldIdCard, msService.getMessage("MISSING_ID_CARD_HAND"));
        Member member = memberService.findOne(user.getId());
        if ("China".equals(member.getCountry().getEnName()) && authType == CredentialsType.CARDED.getOrdinal()) {
            isTrue(ValidateUtil.isChineseName(realName), msService.getMessage("REAL_NAME_ILLEGAL"));
            isTrue(IdcardValidator.isValidate18Idcard(idCard), msService.getMessage("ID_CARD_ILLEGAL"));
        }else{
            isTrue(idCard.length()<20&&idCard.length()>=6,msService.getMessage("ID_CARD_ILLEGAL"));
        }
        isTrue(member.getRealNameStatus() == RealNameStatus.NOT_CERTIFIED, msService.getMessage("REPEAT_REAL_NAME_REQUEST"));
        if (realTimes != 0) {
            isTrue(memberApplicationService.findSuccessRealAuthByIdCard(idCard) < realTimes, msService.getMessage("LIMIT_REAL_NAME_TIMES"));
        }
        CredentialsType credentialsType=CredentialsType.getByValue(authType);
        if(credentialsType==null){
            return MessageResult.error(msService.getMessage("ILLEGAL_AUTHENTICATION_TYPE"));
        }
        MemberApplication memberApplication = new MemberApplication();
        //认证类型
        memberApplication.setType(credentialsType);
        memberApplication.setAuditStatus(AuditStatus.AUDIT_ING);
        memberApplication.setRealName(realName);
        memberApplication.setIdCard(idCard);
        memberApplication.setMember(member);
        memberApplication.setIdentityCardImgFront(idCardFront);
        memberApplication.setIdentityCardImgInHand(handHeldIdCard);
        memberApplication.setIdentityCardImgReverse(idCardBack);
        memberApplication.setCreateTime(new Date());
        memberApplication.setKycStatus(5);
        memberApplicationService.save(memberApplication);
        member.setRealNameStatus(RealNameStatus.AUDITING);
        return MessageResult.success(msService.getMessage("REAL_APPLY_SUCCESS"));
    }


    /**
     * 实名认证（kyc2）
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/kyc/real/name",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "实名认证（二级）")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realApproveVideo(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,@RequestParam("videoStr") String
            videoStr,@RequestParam("random") String random) {
        hasText(videoStr, msService.getMessage("MISSING_URL"));
        Member member = memberService.findOne(user.getId());
        isTrue(member.getKycStatus() == 1 || member.getKycStatus() == 3, msService.getMessage("NO_KYC_VERIFY"));

        //查询待二级审核或二级审核失败的
        List<Integer> status = Arrays.asList(1,3);
        MemberApplication memberApplication = memberApplicationService.findMemberApplicationByKycStatusIn(status,member);
        if (memberApplication == null){
            return MessageResult.error(msService.getMessage("NO_KYC_VERIFY"));
        }
        //认证类型
        memberApplication.setKycStatus(6);
        memberApplication.setVideoUrl(videoStr);
        memberApplication.setVideoRandom(random);
        memberApplicationService.save(memberApplication);
        member.setKycStatus(6);
        memberService.save(member);

        return MessageResult.success(msService.getMessage("KYC_SUBMITTED"));
    }


    /**
     * 查询实名认证情况
     *
     * @param user
     * @return
     */
    @PostMapping("/real/detail")
    @ApiOperation(value = "查询实名认证情况")
    @MultiDataSource(name = "second")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult realNameApproveDetail(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        List<Predicate> predicateList = new ArrayList<>();
        predicateList.add(QMemberApplication.memberApplication.member.eq(member));
        PageResult<MemberApplication> memberApplicationPageResult = memberApplicationService.query(predicateList, null, null);
        MemberApplication memberApplication = new MemberApplication();
        if (memberApplicationPageResult != null && memberApplicationPageResult.getContent() != null
                && memberApplicationPageResult.getContent().size() > 0) {
            memberApplication = memberApplicationPageResult.getContent().get(0);
        }
        MessageResult result = MessageResult.success();
        result.setData(memberApplication);
        return result;
    }

    /**
     * 账户设置
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/account/setting",method = RequestMethod.POST)
    @ApiOperation(value = "账户设置")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult accountSetting(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        hasText(member.getIdNumber(), msService.getMessage("NO_REAL_NAME"));
        hasText(member.getJyPassword(), msService.getMessage("NO_JY_PASSWORD"));
        MemberAccount memberAccount = MemberAccount.builder().alipay(member.getAlipay())
                .aliVerified(member.getAlipay() == null ? IS_FALSE : IS_TRUE)
                .bankInfo(member.getBankInfo())
                .bankVerified(member.getBankInfo() == null ? IS_FALSE : IS_TRUE)
                .wechatPay(member.getWechatPay())
                .wechatVerified(member.getWechatPay() == null ? IS_FALSE : IS_TRUE)
                .realName(member.getRealName())
                .memberLevel(member.getMemberLevel().getOrdinal())
                .build();
        MessageResult result = MessageResult.success();
        result.setData(memberAccount);
        return result;
    }


    /**
     * 设置银行卡
     *
     * @param bindBank
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/bind/bank",method = RequestMethod.POST)
    @ApiOperation(value = "设置银行卡")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindBank(@Valid BindBank bindBank, BindingResult bindingResult, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(member.getBankInfo() == null, msService.getMessage("REPEAT_SETTING"));
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        return doBank(bindBank, user);
    }

    private MessageResult doBank(BindBank bindBank, AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(Md5.md5Digest(bindBank.getJyPassword() + member.getSalt()).toLowerCase().equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        BankInfo bankInfo = new BankInfo();
        bankInfo.setBank(bindBank.getBank());
        bankInfo.setBranch(bindBank.getBranch());
        bankInfo.setCardNo(bindBank.getCardNo());
        member.setBankInfo(bankInfo);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 更改银行卡
     *
     * @param bindBank
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/update/bank",method = RequestMethod.POST)
    @ApiOperation(value = "更改银行卡")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateBank(@Valid BindBank bindBank, BindingResult bindingResult, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        return doBank(bindBank, user);
    }

    /**
     * 绑定阿里
     *
     * @param bindAli
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/bind/ali",method = RequestMethod.POST)
    @ApiOperation(value = "绑定支付宝")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindAli(@Valid BindAli bindAli, BindingResult bindingResult, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(member.getAlipay() == null, msService.getMessage("REPEAT_SETTING"));
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        return doAli(bindAli, user);
    }

    private MessageResult doAli(BindAli bindAli, AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(Md5.md5Digest(bindAli.getJyPassword() + member.getSalt()).toLowerCase().equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        Alipay alipay = new Alipay();
        alipay.setAliNo(bindAli.getAli());
        alipay.setQrCodeUrl(bindAli.getQrCodeUrl());
        member.setAlipay(alipay);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 修改支付宝
     *
     * @param bindAli
     * @param bindingResult
     * @param user
     * @return
     */
    @RequestMapping(value = "/update/ali",method = RequestMethod.POST)
    @ApiOperation(value = "修改支付宝")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateAli(@Valid BindAli bindAli, BindingResult bindingResult, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        return doAli(bindAli, user);
    }

    /**
     * 绑定微信
     *
     * @param bindWechat
     * @param bindingResult
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/bind/wechat",method = RequestMethod.POST)
    @ApiOperation(value = "绑定微信")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult bindWechat(@Valid BindWechat bindWechat, BindingResult bindingResult, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(member.getWechatPay() == null, msService.getMessage("REPEAT_SETTING"));
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        return doWechat(bindWechat, user);
    }

    private MessageResult doWechat(BindWechat bindWechat, AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        isTrue(Md5.md5Digest(bindWechat.getJyPassword() + member.getSalt()).toLowerCase().equals(member.getJyPassword()), msService.getMessage("ERROR_JYPASSWORD"));
        WechatPay wechatPay = new WechatPay();
        wechatPay.setWechat(bindWechat.getWechat());
        wechatPay.setQrWeCodeUrl(bindWechat.getQrCodeUrl());
        member.setWechatPay(wechatPay);
        return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
    }

    /**
     * 修改微信
     *
     * @param bindWechat
     * @param bindingResult
     * @param user
     * @return
     */
    @RequestMapping(value = "/update/wechat",method = RequestMethod.POST)
    @ApiOperation(value = "修改微信")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateWechat(@Valid BindWechat bindWechat, BindingResult bindingResult, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        return doWechat(bindWechat, user);
    }

    @RequestMapping(value = "/delete/bind",method = RequestMethod.POST)
    @ApiOperation(value = "删除收款方式")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deleteBind(String jyPassword,PayMode payMode, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        switch (payMode){
            case BANK:
                BindBank bindBank = new BindBank();
                bindBank.setJyPassword(jyPassword);
                bindBank.setBank(null);
                bindBank.setBranch(null);
                bindBank.setCardNo(null);
                return doBank(bindBank,user);
            case ALIPAY:
                BindAli bindAli = new BindAli();
                bindAli.setJyPassword(jyPassword);
                bindAli.setAli(null);
                bindAli.setQrCodeUrl(null);
                return doAli(bindAli, user);
            case WECHAT:
                BindWechat bindWechat = new BindWechat();
                bindWechat.setJyPassword(jyPassword);
                bindWechat.setWechat(null);
                bindWechat.setQrCodeUrl(null);
                return doWechat(bindWechat,  user);
            default:
                return error(msService.getMessage("RECORD_NOT_EXIST"));
        }
    }

    /**
     * 认证商家申请状态
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/certified/business/status",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "认证商家申请状态")
    @MultiDataSource(name = "second")
    public MessageResult certifiedBusinessStatus(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Member member = memberService.findOne(user.getId());
        CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
        certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus());
        certifiedBusinessInfo.setEmail(member.getEmail());
        certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
        logger.info("会员状态信息:{}", certifiedBusinessInfo);
        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, member.getCertifiedBusinessStatus());
        if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
            logger.info("会员申请商家认证信息:{}", businessAuthApplyList);
            certifiedBusinessInfo.setBusinessAuthApply(businessAuthApplyList.get(0));
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                certifiedBusinessInfo.setCertifiedBusinessStatus(businessAuthApplyList.get(0).getCertifiedBusinessStatus());
                logger.info("会员申请商家认证最新信息:{}", businessAuthApplyList.get(0));
                certifiedBusinessInfo.setDetail(businessAuthApplyList.get(0).getDetail());
            }
        }
        List<BusinessCancelApply> businessCancelApplies = businessCancelApplyService.findByMember(member);
        if (businessCancelApplies != null && businessCancelApplies.size() > 0) {
            if (businessCancelApplies.get(0).getStatus() == RETURN_SUCCESS) {
                if (member.getCertifiedBusinessStatus() != VERIFIED)
                {certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_SUCCESS);}
            } else if (businessCancelApplies.get(0).getStatus() == RETURN_FAILED) {
                certifiedBusinessInfo.setCertifiedBusinessStatus(RETURN_FAILED);
                certifiedBusinessInfo.setReason(businessCancelApplies.get(0).getDetail());
            } else {
                certifiedBusinessInfo.setCertifiedBusinessStatus(CANCEL_AUTH);
            }
        }

        MessageResult result = MessageResult.success();
        result.setData(certifiedBusinessInfo);
        return result;
    }

    /**
     * 认证商家申请
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/certified/business/apply",method =  {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "认证商家申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "telno", value = "电话", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "wechat", value = "微信", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mail", value = "邮箱", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "houseBook", value = "户口本", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "houseCertificate", value = "房产证/租房合同/水电费清单(三选一)", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "video", value = "视频", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "contactPerson", value = "紧急联系人", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "contactPhone", value = "紧急联系人电话", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "contactRelation", value = "紧急联系人与本人关系", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "contactAddress", value = "常驻地址", required = false, dataType = "String", paramType = "query"),
    })
    @Transactional(rollbackFor = Exception.class)
    public MessageResult certifiedBusiness(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, BusinessAuthInfo businessAuthInfo,
                                           @RequestParam Long businessAuthDepositId) {
        Member member = memberService.findOne(user.getId());
        //只有未认证和认证失败的用户，可以发起认证申请
        isTrue(member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.NOT_CERTIFIED)
                || member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.FAILED), msService.getMessage("REPEAT_APPLICATION"));
        isTrue(member.getMemberLevel().equals(MemberLevelEnum.REALNAME), msService.getMessage("NO_REAL_NAME"));
        //hasText(member.getEmail(), msService.getMessage("NOT_BIND_EMAIL"));
        //检查有没有证明
        if(StringUtils.isEmpty(businessAuthInfo.getHouseBook()) || businessAuthInfo.getHouseBook().equalsIgnoreCase("null")
                ||StringUtils.isEmpty(businessAuthInfo.getHouseCertificate())||businessAuthInfo.getHouseCertificate().equalsIgnoreCase("null")
                ||StringUtils.isEmpty(businessAuthInfo.getVideo())||businessAuthInfo.getVideo().equalsIgnoreCase("null")){
            return MessageResult.error(msService.getMessage("APPLY_NOT_NULL"));
        }
        List<BusinessAuthDeposit> depositList = businessAuthDepositService.findAllByStatus(CommonStatus.NORMAL);
        //如果当前有启用的保证金类型，必须选择一种保证金才可以申请商家认证
        BusinessAuthDeposit businessAuthDeposit = null;
        if (depositList != null && depositList.size() > 0) {
            if (businessAuthDepositId == null) {
                return MessageResult.error(msService.getMessage("MUST_SELECT_BUSINESS_DEPOSIT"));
            }
            boolean flag = false;
            for (BusinessAuthDeposit deposit : depositList) {
                if (deposit.getId().equals(businessAuthDepositId)) {
                    businessAuthDeposit = deposit;
                    flag = true;
                }
            }
            if (!flag) {
                return MessageResult.error(msService.getMessage("BUSINESS_DEPOSIT_NOT_FOUND"));
            }
            OtcWallet memberWallet = otcWalletService.findByCoinAndMember(member.getId(),businessAuthDeposit.getCoin());
            if (memberWallet == null){
                return error(msService.getMessage("NO_CASH_ACCOUNT"));
            }
            if (memberWallet.getBalance().compareTo(businessAuthDeposit.getAmount()) < 0) {
                return MessageResult.error(msService.getMessage("BALANCE_RUNNING_LOW"));
            }
            //冻结保证金需要的金额
            memberWallet.setBalance(memberWallet.getBalance().subtract(businessAuthDeposit.getAmount()));
            memberWallet.setFrozenBalance(memberWallet.getFrozenBalance().add(businessAuthDeposit.getAmount()));
            businessAuthInfo.setCoinSymbol(businessAuthDeposit.getCoin().getName());
            businessAuthInfo.setAmount(businessAuthDeposit.getAmount());
        }
        businessAuthInfo.setName(member.getRealName());
        //申请记录
        BusinessAuthApply businessAuthApply = new BusinessAuthApply();
        businessAuthApply.setCreateTime(new Date());
        businessAuthApply.setAuthInfo(JSONObject.toJSONString(businessAuthInfo));
        businessAuthApply.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
        businessAuthApply.setMember(member);
        businessAuthApply.setVerifyLevel(VerifyLevel.NO);
        //不一定会有保证金策略
        if (businessAuthDeposit != null) {
            businessAuthApply.setBusinessAuthDeposit(businessAuthDeposit);
            businessAuthApply.setAmount(businessAuthDeposit.getAmount());
        }
        businessAuthApplyService.create(businessAuthApply);

        member.setCertifiedBusinessApplyTime(new Date());
        member.setCertifiedBusinessStatus(CertifiedBusinessStatus.AUDITING);
        CertifiedBusinessInfo certifiedBusinessInfo = new CertifiedBusinessInfo();
        certifiedBusinessInfo.setCertifiedBusinessStatus(member.getCertifiedBusinessStatus());
        certifiedBusinessInfo.setEmail(member.getEmail());
        certifiedBusinessInfo.setMemberLevel(member.getMemberLevel());
        MessageResult result = MessageResult.success();
        result.setData(certifiedBusinessInfo);
        return result;
    }

    @RequestMapping(value = "/business-auth-deposit/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "查询保证金")
    @MultiDataSource(name = "second")
    public MessageResult listBusinessAuthDepositList() {
        List<BusinessAuthDeposit> depositList = businessAuthDepositService.findAllByStatus(CommonStatus.NORMAL);
        depositList.forEach(deposit -> {
            deposit.setAdmin(null);
        });
        MessageResult result = MessageResult.success();
        result.setData(depositList);
        return result;
    }

    /**
     * 原来的手机还能用的情况下更换手机
     *
     * @param request
     * @param password
     * @param phone
     * @param code
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/change/phone",method = RequestMethod.POST)
    @ApiOperation(value = "原来的手机还能用的情况下更换手机")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(SysConstant.TOKEN_PHONE_UNTIE)
    public MessageResult changePhone(HttpServletRequest request, String password, String phone, String code, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        hasText(password, msService.getMessage("MISSING_LOGIN_PASSWORD"));
        hasText(phone, msService.getMessage("MISSING_PHONE"));
        hasText(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        Member member1 = memberService.findByPhone(phone);
        isTrue(member1 == null, msService.getMessage("PHONE_ALREADY_BOUND"));
        Object cache = redisUtil.get(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
        notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
        if (member.getCountry().getAreaCode().equals("86")) {
            if (!ValidateUtil.isMobilePhone(phone.trim())) {
                return MessageResult.error(msService.getMessage("PHONE_FORMAT_ERROR"));
            }
        }
        if (member.getPassword().equals(Md5.md5Digest(password + member.getSalt()).toLowerCase())) {
            if (!code.equals(cache.toString())) {
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                redisUtil.delete(SysConstant.PHONE_BIND_CODE_PREFIX + phone);
            }
            member.setMobilePhone(phone);
            return MessageResult.success(msService.getMessage("SETTING_SUCCESS"));
        } else {
            request.removeAttribute(SysConstant.SESSION_MEMBER);
            return MessageResult.error(msService.getMessage("PASSWORD_ERROR"));
        }
    }

    /**
     * 申请取消认证商家
     *
     * @return
     */
    @PostMapping("/cancel/business")
    @ApiOperation(value = "申请取消认证商家")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelBusiness(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                        @RequestParam(value = "detail", defaultValue = "") String detail) {
        Member member = memberService.findOne(user.getId());
        logger.info("申请退保，原因={}",detail);
        int advertiseNum = advertiseService.countByMemberAndStatus(member,AdvertiseControlStatus.PUT_ON_SHELVES);
        log.error("advertiseNum={}",advertiseNum);
        if(advertiseNum>0){
            return MessageResult.error(msService.getMessage("ADVERTISE_IN_USE_ERROR"));
        }
        long orderNum = orderService.countByMemberProcessing(member.getId());
        log.error("orderNum={}",orderNum);
        if(orderNum>0){
            return MessageResult.error(msService.getMessage("ADVERTISE_IN_PROGRESS_ERROR"));
        }
        
        if (member.getCertifiedBusinessStatus() == CANCEL_AUTH) {
            return MessageResult.error(msService.getMessage("REFUND_SUBMITTED_ERROR"));
        }
        if (!member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.VERIFIED)/*&&
                !member.getCertifiedBusinessStatus().equals(CertifiedBusinessStatus.RETURN_FAILED)*/) {
            return MessageResult.error(msService.getMessage("NOT_BUSINESS_ERROR"));
        }

        List<BusinessAuthApply> businessAuthApplyList = businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(member, CertifiedBusinessStatus.VERIFIED);
        if (businessAuthApplyList == null || businessAuthApplyList.size() < 1) {
            return MessageResult.error(msService.getMessage("NOT_AUTH_BUSINESS_ERROR"));
        }

        if (businessAuthApplyList.get(0).getCertifiedBusinessStatus() != CertifiedBusinessStatus.VERIFIED) {
            return MessageResult.error(msService.getMessage("AUTH_BUSINESS_STATUS_ERROR"));
        }

        member.setCertifiedBusinessStatus(CANCEL_AUTH);
        log.info("会员状态:{}", member.getCertifiedBusinessStatus());
        memberService.save(member);
        log.info("会员状态:{}", member.getCertifiedBusinessStatus());

        BusinessCancelApply cancelApply = new BusinessCancelApply();
        cancelApply.setDepositRecordId(businessAuthApplyList.get(0).getDepositRecordId());
        cancelApply.setMember(businessAuthApplyList.get(0).getMember());
        cancelApply.setStatus(CANCEL_AUTH);
        cancelApply.setReason(detail);
        log.info("退保申请状态:{}", cancelApply.getStatus());
        businessCancelApplyService.save(cancelApply);
        log.info("退保申请状态:{}", cancelApply.getStatus());

        return MessageResult.success();
    }

    private MessageResult checkCode(String key,Member member,String code,Integer codeMold){
        if(codeType==0){
            String fullKey="";
            if(codeMold==1){
                fullKey=key+member.getEmail();
            }else {
                fullKey=key+member.getMobilePhone();
            }
            Object cache = redisUtil.get(fullKey);
            notNull(cache, msService.getMessage("NO_GET_VERIFICATION_CODE"));
            if (!code.equals(cache.toString())) {
                return MessageResult.error(msService.getMessage("VERIFICATION_CODE_INCORRECT"));
            } else {
                redisUtil.delete(fullKey);
            }
        }else if(codeType==1){
            if(member.getGoogleState()==1) {
                boolean r = GoogleAuthenticatorUtil.checkCodes(code,member.getGoogleKey());
                if (!r) {
                    return MessageResult.error(msService.getMessage("GOOGLE_AUTH_FAILD"));
                }
            }else{
                return MessageResult.error(msService.getMessage("BIND_GOOGLE_FIRST"));
            }
        }
        return MessageResult.success();
    }

    /**
     * 获取视频随机码（六位）
     * @param user
     * @return
     */
    @RequestMapping(value = "/video/random",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取视频随机码")
    public MessageResult videoRandom(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        JSONObject jsonResult = new JSONObject();
        String memberId = user.getId()+"";
        int random = GeneratorUtil.getRandomNumber(100000, 999999);
        jsonResult.put("memberId",memberId);
        jsonResult.put("random",random);
        logger.info("=====获取视频随机码===="+jsonResult.toJSONString());
        MessageResult result=MessageResult.success();
        result.setData(jsonResult);
        return result;
    }

    /**
     * 更改快速提币设置
     *
     * @param isQuick
     * @param user
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/update/isQuick",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "更改快速提币设置")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult updateIsQuick(
            BooleanEnum isQuick,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user){
        Member member = memberService.findOne(user.getId());
        member.setIsQuick(isQuick);
        return success(member);
    }

    /**
     * 更改使用SE抵扣手续费设置
     * @param seFeeSwitch
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/update/seFeeSwitch",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "更改使用SE抵扣手续费设置")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult seFeeSwitch(
            BooleanEnum seFeeSwitch,
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user){
        Member member = memberService.findOne(user.getId());
        member.setSeFeeSwitch(seFeeSwitch.isIs());
        memberService.saveAndFlush(member);
        seFeeChangeRecordService.saveSeFeeChangeRecord(member,member.getSeFeeSwitch()? SeFeeChangeType.SEFEE : SeFeeChangeType.NORMAL, SeFeeChangeWay.MANUAL);
        return success(member);
    }

    /**
     * SE抵扣操作记录
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "seFeeChangeRecord",method = RequestMethod.POST)
    @ApiOperation(value = "SE抵扣操作记录")
    public MessageResult getSeFeeChangeRecord(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,
                                              PageModel pageModel) {
        PageResult<SeFeeChangeRecord> result = seFeeChangeRecordService.query(user.getId(),pageModel.getPageNo(),pageModel.getPageSize());
        return success(result);
    }
}
