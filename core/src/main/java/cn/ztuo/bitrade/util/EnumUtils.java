package cn.ztuo.bitrade.util;

import cn.ztuo.bitrade.service.LocalizationExtendService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnumUtils {
    public static List<Map<String, String>> getEnumInfo(Class<? extends Enum> aClass) {
        if(aClass.getName().startsWith("cn.ztuo.bitrade.")){
            List<Map<String, String>> list = new ArrayList<>();
            LocalizationExtendService localizationExtendService = SpringContextUtil.getBean(LocalizationExtendService.class);
            String locale = LocaleContextHolder.getLocale().toLanguageTag();
            for (Enum anEnum: aClass.getEnumConstants()) {
                String text = localizationExtendService.getLocaleInfo("ENUM", locale,
                        aClass.getName().replace("cn.ztuo.bitrade.",""),anEnum.name());
                Map<String, String> map = new HashMap<>();
                map.put("id",String.valueOf(anEnum.ordinal()));
                map.put("text",StringUtils.defaultString(text, anEnum.name()));
                list.add(map);
            }
            return list;
        }
        return null;
    }

}
