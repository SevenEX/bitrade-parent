package cn.ztuo.bitrade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtcOrderOverview {
    private long successBuyCount;
    private long successSellCount;
    private long failBuyCount30;
    private long failSellCount30;
    private long successBuyCount30;
    private long successSellCount30;
}
