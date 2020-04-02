package cn.ztuo.bitrade.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Locale {
    //英文
    String EN_US = "en-US";
    //简体中文
    String ZH_CN = "zh-CN";
    // 日语
    String JA_JP = "ja-JP";
    // 韩语
    String KO_KR = "ko-KR";
    // 俄语
    String AR_AE = "ar-AE";

    List<String> locales = Arrays.asList(Locale.EN_US, Locale.ZH_CN, Locale.JA_JP, Locale.KO_KR, Locale.AR_AE);

    Map<String,String> Locale_MAP = new HashMap<String , String>(){{
        put(Locale.EN_US, "English");
        put(Locale.ZH_CN, "简体中文");
        put(Locale.JA_JP, "にほんご");
        put(Locale.KO_KR, "한국어");
        put(Locale.AR_AE, "اللغة العربية");
    }};
}
