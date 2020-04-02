package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Seven
 * @date 2019年01月29日
 */
@Builder
@Data
public class WithdrawWalletInfo {
    private String unit;
    /**
     * 阈值
     */
    private BigDecimal threshold;
    /**
     * 最小提币数量
     */
    private BigDecimal minAmount;
    /**
     * 最大提币数量
     */
    private BigDecimal maxAmount;
    private BigDecimal minTxFee;
    private BigDecimal maxTxFee;
    private String nameCn;
    private String name;
    private BigDecimal balance;
    private BooleanEnum canAutoWithdraw;
    private int withdrawScale;
    /**
     * 地址
     */
    private List<Map<String,String>> addresses;
}
