package cn.ztuo.bitrade.controller;

import com.aliyun.oss.common.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/5/11 3:21 PM
 */
@Slf4j
public class TestApi {


    private static final String API_HOST = "39.100.79.158";
    private static final String SIGNATURE_METHOD = "HmacSHA256";
    private static final String SIGNATURE_VERSION = "2";

    private static final ZoneId ZONE_GMT = ZoneId.of("Z");

    private static final SimpleDateFormat DT_FORMAT = new SimpleDateFormat("uuuu-MM-dd'T'HH:mm:ss");


    private static String createSignature(String method, String path, String apiKey, String timeStamp,
                                          Map map, String secretKey) throws Exception{
        StringBuilder sb = new StringBuilder(1024);
        // GET
        sb.append(method.toUpperCase()).append('\n')
                // Host
                .append(API_HOST.toLowerCase()).append('\n')
                // path
                .append(path).append('\n');



        StringJoiner joiner = new StringJoiner("&");
        joiner.add("accessKeyId=" + apiKey)
                .add("signatureMethod=" + SIGNATURE_METHOD)
                .add("signatureVersion=" + SIGNATURE_VERSION)
                .add("timestamp=" + encode(timeStamp));


        //拼接 遍历map
        Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
        while (entries.hasNext()){
            Map.Entry<String, String> entry = entries.next();
            joiner.add(entry.getKey()+"="+entry.getValue());
        }
        log.info("sb={},joiner={}",sb.toString(),joiner.toString());
        return sign(sb.toString() + joiner.toString(), secretKey);
    }

    public static String sign(String message, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            byte[] hash = sha256_HMAC.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message.", e);
        }
    }

    private static String encode(String code) {
        try {
            return URLEncoder.encode(code, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
//    public static void main(String[] args) throws Exception{
//        Date date = new Date();
//        Map<String,String> map = new TreeMap<>();
//        map.put("memberId","9");
//        map.put("direction","SELL");
//        map.put("symbol","BTC/USDT");
//        map.put("price","6750");
//        map.put("amount","1");
//        map.put("type","LIMIT_PRICE");
//        String time = DT_FORMAT.format(date);
//        String signature = createSignature("POST","/user/add_order",
//                "5f79608e-e270-401c-9c13-f7ff8800952d",time,
//                map,"989eb428-af34-49e2-a793-a4f14fc28f44");
//        System.out.println(URLEncoder.encode(signature,"UTF-8"));
//        System.out.println(time);
//    }
}
