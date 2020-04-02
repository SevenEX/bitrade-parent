package cn.ztuo.bitrade.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestUtil {
    public static String remoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    public static Map<String,String> getAreaDetail(String apiUrl,String apiKey,String apiValue){
        JSONObject jsonObject=JSONObject.fromObject(HttpClientUtil.get(apiUrl,apiKey,apiValue));
        log.info("getAreaDetail="+jsonObject.toString());
        Map<String,String> resultMap= (Map<String, String>) JSONObject.toBean(jsonObject,HashMap.class);
        return resultMap;
    }

    public static String remoteWay(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent").toLowerCase();;
        if(userAgent.indexOf("micromessenger")!= -1){
            //微信
            return "Other";
        }else if(userAgent.indexOf("android") != -1){
            //安卓
            return "Android";
        }else if(userAgent.indexOf("iphone") != -1 || userAgent.indexOf("ipad") != -1 || userAgent.indexOf("ipod") != -1){
            //苹果
            return "IOS";
        }else{
            //电脑
            return "WEB";
        }

    }

}
