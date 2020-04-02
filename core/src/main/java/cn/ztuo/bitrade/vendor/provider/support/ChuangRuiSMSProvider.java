package cn.ztuo.bitrade.vendor.provider.support;

import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import cn.ztuo.bitrade.util.MessageResult;
import lombok.extern.slf4j.Slf4j;


/**
 * 创瑞短信接口实现类
 * @author huazhan
 */
@Slf4j
public class ChuangRuiSMSProvider implements SMSProvider {

    private String gateway;
    private String username;
    private String password;
    private String sign;

    public ChuangRuiSMSProvider(String gateway, String username, String password, String sign) {
        this.gateway = gateway;
        this.username = username;
        this.password = password;
        this.sign = sign;
    }

    public static String getName() {
        return "chuangrui";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        log.info("sms content={}", content);
        HttpResponse<String> response = Unirest.post(gateway)
                .field("name", username)
                .field("pwd", password)
                .field("mobile", mobile)
                .field("content", content + "【" + sign + "】")
                .field("time", "")
                .field("type", "pt")
                .field("extno", "")
                .asString();
        log.info(" mobile : " + mobile + "content : " + content);
        log.info("result = {}", response.getBody());
        return parseResult(response.getBody());
    }

    public MessageResult sendLoginMessage(String ip,String phone) throws Exception {
        String content=sendLoginMessage(ip);
        return sendSingleMessage(content,phone);
    }

    private MessageResult parseResult(String result) {
        //返回示例：0,2017110112134171782680251,0,1,0,提交成功
        String[] parts = result.split(",");
        MessageResult mr = new MessageResult(500, "系统错误");
        mr.setCode(Integer.parseInt(parts[0]));
        mr.setMessage(parts[1]);
        return mr;
    }
}
