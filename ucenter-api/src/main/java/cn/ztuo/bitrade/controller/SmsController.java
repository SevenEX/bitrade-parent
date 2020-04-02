package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.Country;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.CountryService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.util.*;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;
import static cn.ztuo.bitrade.util.MessageResult.error;
import static cn.ztuo.bitrade.util.MessageResult.success;

/**
 * @author Seven
 * @date 2019年01月08日
 */
@RestController
@RequestMapping("/mobile")
@Slf4j
@Api(tags = "获取绑定手机号验证码")
public class SmsController {

    @Autowired
    private SMSProvider smsProvider;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MemberService memberService;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;
    @Autowired
    private CountryService countryService;
    @Value("${sms.driver}")
    private String driverName;
    @Value("${sms.isTset}")
    private Boolean isTset;

    /**
     * 绑定手机号验证码
     *
     * @param phone
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/bind/code", method = RequestMethod.POST)
    @ApiOperation("获取绑定手机号验证码")
    public MessageResult setBindPhoneCode(String country, String phone, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Member member = memberService.findOne(user.getId());
        Assert.isNull(member.getMobilePhone(), localeMessageSourceService.getMessage("REPEAT_PHONE_REQUEST"));
        MessageResult result;
        String randomCode = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));

        // 修改所在国家
        if (StringUtils.isNotBlank(country)) {
            Country one = countryService.findOne(country);
            if (one != null) {
                member.setCountry(one);
                memberService.saveAndFlush(member);
            }
        }
        if (!isTset) {
            if (driverName.equalsIgnoreCase("two_five_three")) {
                result = smsProvider.sendVerifyMessage(member.getCountry().getAreaCode() + phone, randomCode);
            } else if ("86".equals(member.getCountry().getAreaCode())) {
                if (!ValidateUtil.isMobilePhone(phone.trim())) {
                    return error(localeMessageSourceService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
                }
                result = smsProvider.sendVerifyMessage(phone, randomCode);
            } else {
                result = smsProvider.sendInternationalMessage(randomCode, member.getCountry().getAreaCode() + phone);
            }
            if (result.getCode() == 0) {
                String key = SysConstant.PHONE_BIND_CODE_PREFIX + phone;
                redisUtil.delete(key);
                // 缓存验证码
                redisUtil.set(key, randomCode, 10, TimeUnit.MINUTES);
                return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
            } else {
                return error(localeMessageSourceService.getMessage("SEND_SMS_FAILED"));
            }
        } else {
            randomCode = "123456";
            String key = SysConstant.PHONE_BIND_CODE_PREFIX + phone;
            redisUtil.delete(key);
            redisUtil.delete(key + "Time");
            // 缓存验证码
            redisUtil.set(key, randomCode, 10, TimeUnit.MINUTES);
            redisUtil.set(key + "Time", new Date(), 10, TimeUnit.MINUTES);
            return success(localeMessageSourceService.getMessage("SEND_SMS_SUCCESS"));
        }
    }
}
