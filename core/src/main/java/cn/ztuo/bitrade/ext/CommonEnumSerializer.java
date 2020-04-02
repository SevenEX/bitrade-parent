package cn.ztuo.bitrade.ext;

import cn.ztuo.bitrade.service.LocalizationExtendService;
import cn.ztuo.bitrade.util.SpringContextUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;

public class CommonEnumSerializer extends JsonSerializer<Enum> {
    private LocalizationExtendService service;
    @Override
    public void serialize(Enum anEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(service == null) {
            service = SpringContextUtil.getBean(LocalizationExtendService.class);
        }
        String text = service.getLocaleInfo("ENUM", locale,
                anEnum.getDeclaringClass().getName().replace("cn.ztuo.bitrade.",""), anEnum.name());
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName("id");
        jsonGenerator.writeNumber(anEnum.ordinal());
        jsonGenerator.writeFieldName("text");
        jsonGenerator.writeString(StringUtils.defaultString(text, anEnum.name()));
        jsonGenerator.writeEndObject();
    }
}
