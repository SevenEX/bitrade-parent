package cn.ztuo.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TradePlateDto {
    int direction;
    BigDecimal maxAmount;
    BigDecimal minAmount;
    BigDecimal highestPrice;
    BigDecimal lowestPrice;
    String symbol;
    List<TradePlateItem> items;
}
