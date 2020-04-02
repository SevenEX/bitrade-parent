package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.core.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum ExchangeOrderType implements BaseEnum {
    /**
     * 市价
     */
    MARKET_PRICE("市价"),
    /**
     * 限价
     */
    LIMIT_PRICE("限价"),
    /**
     * 止盈止损
     */
    CHECK_FULL_STOP("止盈止损");
    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
