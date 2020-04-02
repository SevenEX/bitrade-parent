package cn.ztuo.bitrade.vendor.provider.support;


import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.util.List;


/**
 * 创瑞短信接口实现类
 */
@Slf4j
public class QinPengSMSProvider implements SMSProvider {

    private String gateway;
    private String username;
    private String password;

    public QinPengSMSProvider(String gateway, String username, String password) {
        this.gateway = gateway;
        this.username = username;
        this.password = password;
    }

    public static String getName() {
        return "qinpeng";
    }


    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws Exception {
        log.info("============sms content==========={}", content);

        //组装请求参数
        JSONObject json = new JSONObject();
        json.put("id", 1);
        json.put("method", "send");
        JSONObject params = new JSONObject();
        params.put("userid", username);
        params.put("password", password);
        JSONObject[] phoneSend = new JSONObject[1];
        JSONObject submit = new JSONObject();
        submit.put("content", "您的验证码为" + content + "，十分钟内有效，如非本人操作，请忽略。【BD】");
        submit.put("phone", mobile);

        phoneSend[0] = submit;
        params.put("submit", phoneSend);
        json.put("params", params);
        log.info("==============请求参数：==========" + json);
        String url = gateway + URLEncoder.encode(json.toJSONString(), "UTF-8");
        //发送get请求
        log.info("==============url==========" + url);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        String responseString = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
        log.info("==============短信请求返回体：==========" + responseString);
        return parse2Result(responseString);
    }

    private MessageResult parse2Result(String result){
        //{"result":[{"phone":"15738776414","msgid":"1806282017484877844","return":"0","info":"成功"}],"id":1}
        JSONObject jsonObject = JSONObject.parseObject(result);
        MessageResult mr = new MessageResult(500, "系统错误");
        List<JSONObject> jsonResult = (List<JSONObject>) jsonObject.get("result");
        JSONObject mess = jsonResult.get(0);
        mr.setCode(Integer.parseInt(mess.getString("return")));
        mr.setMessage(mess.getString("info"));
        return mr;
    }

    @Override
    public MessageResult sendLoginMessage(String ip, String phone) throws Exception {
        String content=sendLoginMessage(ip)+"【BD】";
        return sendSingleMessage(content,phone);
    }
}
