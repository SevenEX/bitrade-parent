package cn.ztuo.bitrade.vendor.provider.support;

import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class EmaySMSProvider implements SMSProvider {

    private String gateway;
    private String username;
    private String password;

    public EmaySMSProvider(String gateway, String username, String password) {
        this.gateway = gateway;
        this.username = username;
        this.password = password;
    }

    private static Pattern RESPONSE_PATTERN = Pattern.compile("<response><error>(-?\\d+)</error><message>(.*[\\\\u4e00-\\\\u9fa5]*)</message></response>");


    public static String getName() {
        return "emay";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws UnirestException {
        log.info("sms content={}", content);
        HttpResponse<String> response = Unirest.post(gateway)
                .field("cdkey", username)
                .field("password", password)
                .field("phone", mobile)
                .field("message", content)
                .asString();
        String resultXml = response.getBody();
        log.info(" mobile : " + mobile + "content : " + content);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        String content = formatVerifyCode(verifyCode);
        return sendSingleMessage(mobile, content);
    }
    @Override
    public MessageResult sendLoginMessage(String ip,String phone) throws Exception {
        String content=sendLoginMessage(ip);
        return sendSingleMessage(content,phone);
    }

    private MessageResult parseXml(String xml) {
        xml = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        Matcher matcher = RESPONSE_PATTERN.matcher(xml);
        MessageResult result = new MessageResult(500, "系统错误");
        if (matcher.find()) {
            result.setCode(Integer.parseInt(matcher.group(1)));
            result.setMessage(matcher.group(2));
        }
        return result;
    }
}
