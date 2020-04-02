package cn.ztuo.bitrade.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 撮合交易信息
 */
@Data
public class ExchangeTrade implements Serializable{
    private String symbol;
    private BigDecimal price;
    private String priceStr;
    private BigDecimal amount;
    private String amountStr;
    private BigDecimal buyTurnover;
    private BigDecimal sellTurnover;
    private ExchangeOrderDirection direction;
    private Boolean isBuyerMaker;
    private String buyOrderId;
    private String sellOrderId;
    private Long time;

    private String senderUuid;

    @Override
    public String toString() {
        return  JSON.toJSONString(this);
    }
}
