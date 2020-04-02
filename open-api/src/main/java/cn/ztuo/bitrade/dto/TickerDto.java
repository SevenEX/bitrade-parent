package cn.ztuo.bitrade.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author Zane
 */
@Data
@Builder
public class TickerDto {
    private long time;
    private String symbol;
    private String volume;
    private String quoteVolume;
    private String lastPrice;
    private String highPrice;
    private String lowPrice;
    private String openPrice;
}
