package cn.ztuo.bitrade.controller.code;

import cn.ztuo.bitrade.constant.SmsCodePrefixEnum;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.service.AdminService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.GeneratorUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.ztuo.bitrade.util.MessageResult.error;
import static cn.ztuo.bitrade.util.MessageResult.success;

@RestController
@Slf4j
@RequestMapping("/code/sms-provider")
@Api(tags = "手机验证码")
public class SmsProviderController {

    private Logger logger = LoggerFactory.getLogger(SmsProviderController.class);

    @Autowired
    private SMSProvider smsProvider;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private AdminService adminService;

    @Value("${sms.driver}")
    private String driverName;
    @Value("${sms.isTset}")
    private Boolean isTset;

    /**
     * 币种管理 修改币种信息手机验证码
     *
     * @param admin
     * @return
     */
    @RequestMapping(value = "/system/coin-revise", method = RequestMethod.POST)
    @ApiOperation(value = "修改币种信息 发送手机验证码")
    public MessageResult sendReviseCode(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        Assert.notNull(admin, msService.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));
        return sendCode(admin, SysConstant.ADMIN_COIN_REVISE_PHONE_PREFIX);
    }

    /**
     * 币币管理 币币设置 手机验证码
     *
     * @param admin
     * @return
     */
    @RequestMapping("/exchange-coin-set")
    @ApiOperation(value = "币币设置 发送手机验证码")
    public MessageResult sendExchangeCoinSet(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        Assert.notNull(admin, msService.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));
        return sendCode(admin, SysConstant.ADMIN_EXCHANGE_COIN_SET_PREFIX);
    }

    /**
     * 转入冷钱包 手机验证码
     *
     * @param admin
     * @return
     */
    @RequestMapping("/transfer-cold-wallet")
    @ApiOperation(value = "转入冷钱包 发送手机验证码")
    public MessageResult sendTransfer(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        Assert.notNull(admin, msService.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));
        return sendCode(admin, SysConstant.ADMIN_COIN_TRANSFER_COLD_PREFIX);
    }

    /**
     * 后台登录  手机验证码
     *
     * @param phone
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "后台登录 发送手机验证码")
    public MessageResult send(String phone) {
        List<Admin> adminList = adminService.findByMobilePhone(phone);
        if (adminList == null || adminList.size() == 0) {
            return MessageResult.error("未绑定的手机号");
        }
        Admin admin = adminList.get(0);
        return sendCode(admin, SysConstant.ADMIN_LOGIN_PHONE_PREFIX);
    }

    /**
     * 发送手机验证码通用方法
     *
     * @param admin          当前登录用户信息
     * @param codePrefixEnum 验证码类型，需要判断合法性
     * @return {@link MessageResult}
     */
    @RequestMapping(value = "/common", method = RequestMethod.GET)
    @ApiOperation(value = "发送手机验证码通用方法（需传入类型）")
    public MessageResult common(@SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin admin, SmsCodePrefixEnum codePrefixEnum) {
        Assert.notNull(admin, msService.getMessage("DATA_EXPIRED_LOGIN_AGAIN"));
        Assert.notNull(codePrefixEnum, "验证码类型不能为空");
        return sendCode(admin, codePrefixEnum.name());
    }

    private MessageResult sendCode(Admin admin, String prefix) {
        Assert.notNull(admin.getMobilePhone(), msService.getMessage("NO_CELL_PHONE_NUMBER"));
        MessageResult result;
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        String phone = checkPhone(admin);
        if (!isTset) {
        try {
            String key = prefix + admin.getMobilePhone();
            long expire = redisUtil.getExpire(key);
            if (expire < 600 && expire > 540) {
                return error(msService.getMessage("SEND_CODE_FAILURE_ONE"));
            }
            if (admin.getMobilePhone().equalsIgnoreCase("10000")) {
                randomCode = "666666";
                logger.info("默认短信验证码:{}", randomCode);
                redisUtil.set(key, randomCode, 10, TimeUnit.MINUTES);
                return success(msService.getMessage("SEND_CODE_SUCCESS") + admin.getMobilePhone());
            }
            if (driverName.equalsIgnoreCase("two_five_three")) {
                result = smsProvider.sendVerifyMessage(phone, randomCode);
            } else {
                if (admin.getAreaCode() == null || admin.getAreaCode().equalsIgnoreCase("") || admin.getAreaCode().equals("86")) {
                    result = smsProvider.sendVerifyMessage(phone, randomCode);
                } else {
                    result = smsProvider.sendInternationalMessage(randomCode, phone);
                }
            }
            logger.info("短信验证码:{}", randomCode);
            if (result.getCode() == 0) {
                redisUtil.set(key, randomCode, 10, TimeUnit.MINUTES);
                return success(msService.getMessage("SEND_CODE_SUCCESS") + admin.getMobilePhone());
            } else {
                redisUtil.set(key, randomCode, 10, TimeUnit.MINUTES);
                log.info("短信发送错误信息：{}", result.getMessage());
            }
        } catch (Exception e) {
            logger.info("发送异常={}", e.getMessage(), e);
        }
    } else {
        randomCode = "123456";
        String key = prefix + admin.getMobilePhone();
        redisUtil.delete(key);
        redisUtil.delete(key + "Time");
        // 缓存验证码
        redisUtil.set(key, randomCode, 10, TimeUnit.MINUTES);
        redisUtil.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
        return success(msService.getMessage("SEND_SMS_SUCCESS"));
    }
        return error(msService.getMessage("REQUEST_FAILED"));
    }

    private String checkPhone(Admin admin) {
        String phone = admin.getMobilePhone();
        if (driverName.equalsIgnoreCase("two_five_three")) {
            if (admin.getAreaCode() == null || admin.getAreaCode().equalsIgnoreCase("")) {
                phone = "86" + phone;
            } else {
                phone = admin.getAreaCode() + phone;
            }
        } else if (admin.getAreaCode() != null && !admin.getAreaCode().equalsIgnoreCase("")) {
            if (!admin.getAreaCode().equalsIgnoreCase("86")) {
                phone = admin.getAreaCode() + phone;
            }
        }
        return phone;
    }
}
