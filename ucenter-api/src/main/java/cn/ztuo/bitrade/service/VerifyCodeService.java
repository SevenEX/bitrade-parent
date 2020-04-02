package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.RedissonUtil;
import cn.ztuo.bitrade.util.ValidateUtil;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.google.common.collect.ImmutableSet;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerifyCodeService extends BaseService {
    /**
     * 是否是测试环境
     */
    @Value("${sms.isTset}")
    private Boolean isTset;
    @Value("${spring.mail.username}")
    private String from;
    @Value("${bdtop.system.host}")
    private String host;
    @Value("${bdtop.system.name}")
    private String company;

    @Autowired
    private JavaMailSender javaMailSender;

    private static Set<String> AVAILABLE_TYPE = ImmutableSet.of(
            SysConstant.TOKEN_ADD_ADDRESS, SysConstant.TOKEN_RESET_PASSWORD, SysConstant.TOKEN_RESET_GOOGLE_AUTH,
            SysConstant.TOKEN_REGISTER_MAIL, SysConstant.TOKEN_RESET_TRANS_PASSWORD, SysConstant.TOKEN_REGISTER_PHONE,
            SysConstant.TOKEN_WITHDRAW_AUTH, SysConstant.TOKEN_SWITCH_VERIFY, SysConstant.TOKEN_EMAIL_BIND,
            SysConstant.TOKEN_EMAIL_UNTIE, SysConstant.TOKEN_PHONE_UNTIE, SysConstant.TOKEN_EMAIL_BIND, SysConstant.TOKEN_PHONE_BIND,SysConstant.TOKEN_API_BIND
    );

    @Autowired
    private SMSProvider smsProvider;

    //普通短信
    public void sendSMSCode(String phone, String areaCode, String type) throws Exception {
        Assert.isTrue(AVAILABLE_TYPE.contains(type), "Type ERROR");
        RMapCache<String, VerifyCodeValue> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.PHONE_VERIFY_CODE_MAP_CACHE + type);
        int ttl = 600;
        int maxRetryCount = 20;
        VerifyCodeValue smspama;
        if (isTset) {
            smspama = new VerifyCodeValue("123456", System.currentTimeMillis(), maxRetryCount);
        } else {
            int minRetryInterval = 60;
            long remainTimeToLive = mapCache.remainTimeToLive(phone);
            Assert.isTrue(remainTimeToLive < 0 || (ttl - remainTimeToLive / 1000) >= minRetryInterval, () -> msService.getMessage("FREQUENTLY_REQUEST"));
            smspama = mapCache.getOrDefault(phone, new VerifyCodeValue(getSmsCode(6)));
            smspama.sentTimeStamp = System.currentTimeMillis();
            smspama.retryCount = maxRetryCount;
            if (areaCode.equals("86")) {
                Assert.isTrue(ValidateUtil.isMobilePhone(phone.trim()), () -> msService.getMessage("PHONE_EMPTY_OR_INCORRECT"));
                smsProvider.sendVerifyMessage(phone, smspama.code);
            } else {
                smsProvider.sendInternationalMessage(smspama.code, areaCode + phone);
            }
        }
        mapCache.fastPut(phone, smspama, ttl, TimeUnit.SECONDS);
    }

    /**
     * 删除短信验证信息；
     *
     * @param phone
     */
    public void removeSMSCode(String phone, String type) {
        RMapCache<String, VerifyCodeValue> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.PHONE_VERIFY_CODE_MAP_CACHE + type);
        mapCache.remove(phone);
    }

    /**
     * 验证码手机验证码
     *
     * @param phone
     * @param code
     * @return
     */
    public void checkSMS(String phone, String type, String code) {
        Assert.isTrue(!StringUtils.isEmpty(code), msService.getMessage("MISSING_VERIFICATION_CODE"));
        String num = getCode(RedissonKeyConstant.PHONE_VERIFY_CODE_MAP_CACHE, phone, type);
        Assert.isTrue(StringUtils.equals(code, num), () -> msService.getMessage("VERIFICATION_CODE_INCORRECT"));
    }

    private String getCode(String key, String accountName, String type) {
        RMapCache<String, VerifyCodeValue> codeMap = RedissonUtil.getMapCache(key + type);
        VerifyCodeValue code = codeMap.get(accountName);
        Assert.notNull(code, () -> msService.getMessage("NO_GET_VERIFICATION_CODE"));
        code.retryCount--;
        Assert.isTrue(code.retryCount >= 0, () -> {
            codeMap.remove(accountName);
            return msService.getMessage("NO_GET_VERIFICATION_CODE");
        });
        int ttl = (int) (600000 - (new Date().getTime() - code.sentTimeStamp));
        if(ttl < 0)
            return code.getCode();
        codeMap.fastPut(accountName, code, ttl, TimeUnit.MILLISECONDS);
        return code.getCode();
    }

    /**
     * 获取手机验证码
     *
     * @param size 位数
     * @return
     */
    public static String getSmsCode(int size) {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    @Data
    @AllArgsConstructor
    private static class VerifyCodeValue implements Serializable {
        private String code;
        private long sentTimeStamp;
        private int retryCount;
        public VerifyCodeValue(String code){
            this.code = code;
        }
    }

    @Async
    public void sentEmailCode(String email, String type) throws Exception {
        Assert.isTrue(AVAILABLE_TYPE.contains(type), "Type ERROR");
        RMapCache<String, VerifyCodeValue> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.EMAIL_VERIFY_CODE_MAP_CACHE + type);
        int ttl = 1800;
        int maxRetryCount = 20;
        VerifyCodeValue smspama;
        if (isTset) {
            smspama = new VerifyCodeValue("123456", new Date().getTime(), maxRetryCount);
        } else {
            int minRetryInterval = 60;
            long remainTimeToLive = mapCache.remainTimeToLive(email);
            Assert.isTrue(remainTimeToLive < 0 || (ttl - remainTimeToLive / 1000) >= minRetryInterval, () -> msService.getMessage("FREQUENTLY_REQUEST"));
            smspama = mapCache.getOrDefault(email, new VerifyCodeValue(getSmsCode(6)));
            smspama.sentTimeStamp = new Date().getTime();
            smspama.retryCount = maxRetryCount;
            sendMail(email, smspama.code);
        }
        mapCache.fastPut(email, smspama, ttl, TimeUnit.SECONDS);
    }

    private void sendMail(String email, String code) throws MessagingException, IOException, TemplateException {
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
    }

    /**
     * 验证码邮箱验证码
     *
     * @param email
     * @param code
     * @return
     */
    public void checkEmail(String email, String type, String code) {
        Assert.isTrue(!StringUtils.isEmpty(code), msService.getMessage("MISSING_VERIFICATION_CODE"));
        String num = getCode(RedissonKeyConstant.EMAIL_VERIFY_CODE_MAP_CACHE, email, type);
        Assert.isTrue(StringUtils.equals(code, num), () -> msService.getMessage("VERIFICATION_CODE_INCORRECT"));
    }

    public void removeEmailCode(String email, String type) {
        RMapCache<String, VerifyCodeValue> mapCache = RedissonUtil.getMapCache(RedissonKeyConstant.EMAIL_VERIFY_CODE_MAP_CACHE + type);
        mapCache.remove(email);
    }
}
