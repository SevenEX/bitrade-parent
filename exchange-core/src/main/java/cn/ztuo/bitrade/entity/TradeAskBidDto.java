package cn.ztuo.bitrade.entity;

import lombok.Data;

@Data
public class TradeAskBidDto<T> {
    private T ask;
    private T bid;
}
