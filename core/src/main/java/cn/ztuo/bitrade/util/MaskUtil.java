package cn.ztuo.bitrade.util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;

/**
 * 掩码工具类
 * @author Dabria_ly
 *
 */
public class MaskUtil {
    /**
     * 掩码手机号
     * @param mobile
     * @return
     */
    public static String maskMobile(String mobile) {
        if(StringUtils.isEmpty(mobile)){
            return mobile;
        }
        int cutBegin = mobile.length() * 3 / 11;
        int cutEnd =  mobile.length() * 7 / 11;
        if(cutEnd < cutBegin){
            return mobile;
        }
        return StringUtils.rightPad(StringUtils.left(mobile, cutBegin), cutEnd, "*") + StringUtils.right(mobile, mobile.length() - cutEnd);
    }

    /**
     * 掩码邮箱
     * @param email
     * @return
     */
    public static String maskEmail(String email) {
        if(StringUtils.isEmpty(email)){
            return email;
        }
        int length = email.indexOf("@");
        if(length <= 0){
            return email;
        }
        int cutBegin = length / 2;
        return StringUtils.rightPad(StringUtils.left(email, cutBegin), length, "*") + StringUtils.right(email, email.length() - length);
    }

    public static void main(String[] args) {
        System.out.println(maskMobile("1"));
        System.out.println(maskMobile("12"));
        System.out.println(maskMobile("123"));
        System.out.println(maskMobile("1234"));
        System.out.println(maskMobile("12345"));
        System.out.println(maskMobile("123456"));
        System.out.println(maskMobile("1234567"));
        System.out.println(maskMobile("12345678"));
        System.out.println(maskMobile("123456789"));
        System.out.println(maskMobile("1234567890"));
        System.out.println(maskMobile("12345678901"));
        System.out.println(maskEmail("1@qq.com"));
        System.out.println(maskEmail("12@qq.com"));
        System.out.println(maskEmail("123@qq.com"));
        System.out.println(maskEmail("1234@qq.com"));
        System.out.println(maskEmail("12345@qq.com"));
        System.out.println(maskEmail("123456@qq.com"));
        System.out.println(maskEmail("1234567@qq.com"));
        System.out.println(maskEmail("12345678@qq.com"));
        System.out.println(maskEmail("123456789@qq.com"));
        System.out.println(maskEmail("1234567890@qq.com"));
        System.out.println(maskEmail("12345678901@qwertyuiop.asdfghjkl"));
    }
}