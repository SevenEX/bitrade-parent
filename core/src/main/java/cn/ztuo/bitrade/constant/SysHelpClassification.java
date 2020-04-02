package cn.ztuo.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author MrGao
 * @description
 * @date 2018/1/9 9:42
 */
@AllArgsConstructor
@Getter
public enum SysHelpClassification implements BaseEnum {

    HELP("新手入门"),
    
    FAQ("常见问题"),

    RECHARGE("充值指南"),

    TRANSACTION("交易指南"),

    QR_CODE("APP二维码");

    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
