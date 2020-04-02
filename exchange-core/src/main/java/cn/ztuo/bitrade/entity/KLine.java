package cn.ztuo.bitrade.entity;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class KLine {
    public KLine(){

    }

    public KLine(String period){
        this.period = period;
    }
    //交易对 ETH/USDT
    private String symbol ;
    //开盘价
    private BigDecimal openPrice = BigDecimal.ZERO;
    //最高价
    private BigDecimal highestPrice  = BigDecimal.ZERO;
    //最低价
    private BigDecimal lowestPrice  = BigDecimal.ZERO;
    // 收盘价
    private BigDecimal closePrice  = BigDecimal.ZERO;
    //时间戳
    private long time;
    //单位 1min 5min
    private String period;
    /**
     * 成交笔数
     */
    private int count;
    /**
     * 成交量
     */
    private BigDecimal volume = BigDecimal.ZERO;
    /**
     * 成交额
     */
    private BigDecimal turnover = BigDecimal.ZERO;
}
