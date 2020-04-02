package cn.ztuo.bitrade.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "exchange_order_detail")
public class ExchangeOrderDetail {
    private String orderId;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal turnover;
    private BigDecimal fee;
    private Long memberId;
    private Long dealMemberId;
    private String feeSymbol;
    //成交时间
    private long time;
}
