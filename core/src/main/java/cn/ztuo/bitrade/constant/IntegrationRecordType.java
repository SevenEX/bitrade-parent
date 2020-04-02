package cn.ztuo.bitrade.constant;

import cn.ztuo.bitrade.core.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 积分赠送类型
 * @author MrGao
 * @date 2019/04/25
 */
@AllArgsConstructor
@Getter
public enum IntegrationRecordType implements BaseEnum {

    /**
     * 推广
     */
    PROMOTION_GIVING("推广"),
    /**
     * 活动
     */
    LEGAL_RECHARGE_GIVING("法币充值赠送"),
    /**
     * 分红
     */
    COIN_RECHARGE_GIVING("币币充值赠送");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
