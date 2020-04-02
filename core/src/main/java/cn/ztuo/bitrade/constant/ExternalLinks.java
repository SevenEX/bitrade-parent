package cn.ztuo.bitrade.constant;

import cn.ztuo.bitrade.core.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
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
public enum ExternalLinks implements BaseEnum {

    ORGAN("机构合作"),
    BUSINESS("商务合作"),
    CUSTOMER("客服邮箱"),
    APPLY("上币申请"),
    ABOUTUS("关于我们"),
    SERVICE("服务协议"),
    PRIVACY("隐私政策"),
    RATEEXPLAIN("费率说明"),
    CONTACTUS("联系我们"),
    INTRODUCE("数字资产介绍"),

    ORGAN_NAME("机构合作"),
    BUSINESS_NAME("商务合作"),
    CUSTOMER_NAME("客服邮箱"),
    APPLY_NAME("上币申请"),
    ABOUTUS_NAME("关于我们"),
    SERVICE_NAME("服务协议"),
    PRIVACY_NAME("隐私政策"),
    RATEEXPLAIN_NAME("费率说明"),
    CONTACTUS_NAME("联系我们"),
    INTRODUCE_NAME("数字资产介绍"),
    ;

    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
