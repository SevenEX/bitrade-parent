package cn.ztuo.bitrade.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author Zane
 */
@Data
@Builder
public class TradeDto {
    private String price;
    private long time;
    private String qty;
    private Boolean isBuyerMaker;
}
