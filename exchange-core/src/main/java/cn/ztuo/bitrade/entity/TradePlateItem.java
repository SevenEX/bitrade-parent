package cn.ztuo.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradePlateItem {
    private BigDecimal price;
    private BigDecimal amount;
    private String priceStr;
    private String amountStr;
}
