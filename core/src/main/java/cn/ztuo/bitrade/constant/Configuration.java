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
public enum Configuration implements BaseEnum {

    TUTORIAL("新手教程"),

    TRADINGRULES("交易规则"),

    HELP("帮助中心"),

    PRIVACY("隐私政策"),

    ADVERTIS("广告服务协议"),

    INVITATION("邀请返佣奖励细则"),

    SE_WEB("SE抵扣说明(WEB端)"),

    SE_MOBILE("SE抵扣说明(WEB端)"),

    GRADE("等级费率说明");

    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
