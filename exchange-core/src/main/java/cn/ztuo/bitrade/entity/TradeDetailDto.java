package cn.ztuo.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class TradeDetailDto {
    private Map<BigDecimal,MergeOrder> limitPriceQueue;
    private List<ExchangeOrder> marketPriceQueue;
}
