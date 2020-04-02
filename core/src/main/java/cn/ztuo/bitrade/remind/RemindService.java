package cn.ztuo.bitrade.remind;

import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.Order;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件 or 短信 提醒
 *
 * @author dz
 */
@Service
@Slf4j
public class RemindService {

    @Resource
    private SMSProvider smsProvider;
    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${bdtop.system.name}")
    private String company;

    @Async
    public void sendInfo(Member member, Order order, RemindType type) {
        log.info("进入短信邮件提醒逻辑 ↓↓↓，type：{}，Email：{}，Tel：{}", type,
                member.getEmail(), member.getMobilePhone());
        boolean emailEnable = "0".equals(member.getEmailRemind())
                && !StringUtils.isEmpty(member.getEmail());
        boolean smsEnable = "0".equals(member.getSmsRemind())
                && !StringUtils.isEmpty(member.getMobilePhone());
        // 买家已付款
        // 卖家已发币
        // 申诉 取消
        if (smsEnable) {
            log.info("开始发送短信提醒，type：{}", type);
            boolean is86 = "86".equals(member.getCountry().getAreaCode());
            Map<String, String> map = getSmsMap(member, order, type);
            if (type.equals(RemindType.RELEASE)) {
                log.info("打印参数Map:");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    log.info(entry.getKey() + "<<:>>" + entry.getValue());
                }
            }
            sendCloudSms(member, getTemplateId(type, is86), map);
        }
        if (emailEnable) {
            log.info("开始发送邮件提醒，type：{}", type);
            String html = emailBuild(getParamMap(member, order, type), type);
            sendEmail(member.getEmail(), html);
        }
    }


    @Async
    public void sendAdminInfo(Member member, String html, int templateId, Map<String, String> map) {
        boolean emailEnable = "0".equals(member.getEmailRemind())
                && !StringUtils.isEmpty(member.getEmail());
        boolean smsEnable = "0".equals(member.getSmsRemind())
                && !StringUtils.isEmpty(member.getMobilePhone());
        if (smsEnable) {
            log.info("开始发送 后台申诉完成 短信提醒");
            sendCloudSms(member, templateId, map);
        }
        if (emailEnable) {
            log.info("开始发送 后台申诉完成 短信提醒");
            sendEmail(member.getEmail(), html);
        }
    }

    private static String getTail4(String s) {
        if (4 >= s.length()) {
            return s;
        }
        return s.substring(s.length() - 4);
    }

    private void sendCloudSms(Member member, int templateId, Map<String, String> map) {
        MessageResult result;
        // 国内短信
        boolean is86 = "86".equals(member.getCountry().getAreaCode());
        // 非国内号码加区号
        String phone = is86 ? member.getMobilePhone() :
                member.getCountry().getAreaCode() + member.getMobilePhone();
        try {
            result = smsProvider.sendTemplateMessage(templateId, phone, !is86, map);
            if (result.getCode() == 0) {
                log.info("提醒短信发送成功");
            } else {
                log.info("提醒短信发送失败：{}", result);
            }
        } catch (Exception e) {
            log.info("提醒短信发送出错");
            log.error(e.getLocalizedMessage(), e);
        }
    }


    private static String getHalf(String s, int x) {
        if (x == 0) {
            return s.substring(0, s.length() / 2);
        } else {
            return s.substring(s.length() / 2);
        }
    }

    private Map<String, Object> getParamMap(Member member, Order order, RemindType type) {
        Map<String, Object> map = new HashMap<>(4);
        switch (type) {
            case PAY:
                map.put("name", member.getUsername());
                break;
            case RELEASE:
                map.put("orderSn", getTail4(order.getOrderSn()));
                map.put("amount", order.getNumber());
                map.put("unit", order.getCoin().getUnit());
                break;
            case APPEAL:
                map.put("orderSn", order.getOrderSn());
                map.put("name", member.getUsername());
                break;
            case CANCEL:
                map.put("orderSn", order.getOrderSn());
                break;
            default:
                break;
        }
        return map;
    }

    private Map<String, String> getSmsMap(Member member, Order order, RemindType type) {
        String orderSn = order.getOrderSn();
        switch (type) {
            case ORDER:
                return ImmutableMap.of("id", order.getAdvertiseId().toString(),"account",order.getCustomerName());
            case PAY:
                return ImmutableMap.of("name", member.getUsername());
            case RELEASE:
                String amount = String.valueOf(order.getNumber().setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue());
                return ImmutableMap.of("id", getTail4(order.getOrderSn()),
                        "amount", amount,
                        "unit", order.getCoin().getUnit());
            case APPEAL:
            case CANCEL:
                return ImmutableMap.of("ids", getHalf(orderSn, 0), "ide", getHalf(orderSn, 1));
            default:
                return ImmutableMap.of();
        }
    }

    private int getTemplateId(RemindType type, boolean is86) {
        int id = 0;
        switch (type) {
            case ORDER:
                id = is86 ? 39145 : 39147;
                break;
            case PAY:
                id = is86 ? 42325 : 42326;
                break;
            case RELEASE:
                id = is86 ? 42327 : 42328;
                break;
            case APPEAL:
                id = is86 ? 42342 : 42343;
                break;
            case CANCEL:
                id = is86 ? 42323 : 42324;
                break;
            default:
                break;
        }
        return id;
    }

    /**
     * 根据类型和参数构造邮件内容
     *
     * @param model 参数Map
     * @param type  提醒类型
     * @return String 邮件内容
     */
    private String emailBuild(Map<String, Object> model, RemindType type) {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = null;
        String html = null;
        try {
            if (RemindType.PAY.equals(type)) {
                template = cfg.getTemplate("otc-pay.ftl");
            } else if (RemindType.RELEASE.equals(type)) {
                template = cfg.getTemplate("otc-release.ftl");
            } else if (RemindType.APPEAL.equals(type)) {
                template = cfg.getTemplate("otc-appeal.ftl");
            } else if (RemindType.CANCEL.equals(type)) {
                template = cfg.getTemplate("otc-cancel.ftl");
            }
            if (template == null) {
                return null;
            }
            html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return html;
    }

    /**
     * 邮件发送
     *
     * @param toEmail 目的地址
     * @param html    邮件内容
     */
    private void sendEmail(String toEmail, String html) {
        if (html == null) {
            log.info("发送邮件失败：构建邮件内容为空");
            return;
        }
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(company);
            helper.setText(html, true);
            //发送邮件
            log.info("send email for {},content:{}", toEmail, html);
            javaMailSender.send(mimeMessage);
            log.info("邮件发送成功！");
        } catch (Exception e) {
            log.info("邮件发送失败：");
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
