package cn.ztuo.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Seven
 * @date 2019年03月08日
 */
@AllArgsConstructor
@Getter
public enum PromotionRewardType implements BaseEnum {
    /**
     * 推广注册
     */
    REGISTER("推广注册"),
    /**
     * 法币推广交易
     */
    TRANSACTION("法币推广交易"),
    /**
     * 币币交易
     */
    EXCHANGE_TRANSACTION("币币推广交易");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
