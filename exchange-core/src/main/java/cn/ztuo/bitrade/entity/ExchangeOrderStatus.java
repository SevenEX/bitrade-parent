package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.core.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum ExchangeOrderStatus implements BaseEnum {
    /**
     * 交易中
     */
    TRADING("交易中"),
    /**
     * 完成
     */
    COMPLETED("完成"),
    /**
     * 取消
     */
    CANCELED("取消"),
    /**
     * 超时
     */
    OVERTIMED("超时"),
    /**
     *等待触发
     */
    WAITING_TRIGGER("等待触发")
    ;
    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
