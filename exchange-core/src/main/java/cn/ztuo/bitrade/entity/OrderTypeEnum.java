package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrderTypeEnum {
    OTC("法币交易"),EXCHANGE("币币交易");
    @Setter
    private String cnName;

    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
