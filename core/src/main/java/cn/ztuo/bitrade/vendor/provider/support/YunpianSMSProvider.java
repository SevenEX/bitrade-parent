package cn.ztuo.bitrade.vendor.provider.support;

import com.mashape.unirest.http.exceptions.UnirestException;
import cn.ztuo.bitrade.util.HttpSend;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author tansitao
 * @time 2018-04-05
 * 云片短信验证码发送类
 */
@Slf4j
public class YunpianSMSProvider implements SMSProvider {

    private String gateway;
    private String apikey;

    public YunpianSMSProvider(String gateway, String apikey) {
        this.gateway = gateway;
        this.apikey = apikey;
    }

    private static Pattern RESPONSE_PATTERN = Pattern.compile("<response><error>(-?\\d+)</error><message>(.*[\\\\u4e00-\\\\u9fa5]*)</message></response>");


    public static String getName() {
        return "yunpian";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws UnirestException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("text", content);
        params.put("mobile", mobile);
        log.info("yunpianParameters====", params.toString());
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

//    public static void main(String[] args) {
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("apikey", "5a3a506f2b8a1074a3618890b8951e71");
//        params.put("text", "【coinmany】您的验证码是123456，请按页面提示填写，切勿泄露于他人。");
//        params.put("mobile", "15738776414");
//        log.info("yunpianParameters====", params.toString());
//        String resultXml= HttpSend.yunpianPost("https://sms.yunpian.com/v2/sms/single_send.json", params);
//        log.info("result = {}", resultXml);
//    }

    @Override
    public MessageResult sendInternationalMessage(String code, String phone) throws IOException, DocumentException {
        code = String.format("【coinmany】Your verification code is %s", code);
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", apikey);
        params.put("text", code);
        params.put("mobile", phone);
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    /**
     * 获取验证码信息格式
     *
     * @param code
     * @return
     */
    @Override
    public String formatVerifyCode(String code) {
        return String.format("【coinmany】您的验证码为：%s，请按页面提示填写，切勿泄露于他人。", code);
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        String content = formatVerifyCode(verifyCode);
        return sendSingleMessage(mobile, content);
    }

    public MessageResult sendLoginMessage(String ip,String phone) throws Exception {
        String content=sendLoginMessage(ip);
        return sendSingleMessage(content,phone);
    }

    private MessageResult parseXml(String xml) {
        JSONObject myJsonObject = JSONObject.fromObject(xml);
        int code = myJsonObject.getInt("code");
        MessageResult result = new MessageResult(500, "系统错误");
        if(code == 0)
        {
            result.setCode(code);
            result.setMessage(myJsonObject.getString("msg"));
        }
        return result;
    }
}
