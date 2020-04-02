package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author MrGao
 * @description 杠杆钱包VO
 * @date 2019/02/22 17:27
 */
@Data
@Embeddable
public class LeverWalletVO {
    public static final String btcUnit="BTC";

    private Long memberId;
    private String symbol;
    /**
     * 风险率
     */
    private BigDecimal riskRate;
    /**
     * 基准币可借贷
     */
    private BigDecimal baseCanLoan;
    /**
     * 交易币可借贷
     */
    private BigDecimal coinCanLoan;
    /**
     * 基准币借贷数量
     */
    private BigDecimal baseLoanCount;
    /**
     * 交易币借贷数量
     */
    private BigDecimal coinLoanCount;
    /**
     * 基准币利息
     */
    private BigDecimal baseAccumulativeCount;
    /**
     * 交易币利息
     */
    private BigDecimal coinAccumulativeCount;
    /**
     * 杠杆钱包
     */
    private List<LeverWallet> leverWalletList;
//    /**
//     * btc币种
//     */
//    private Coin btcCoin;
    /**
     * 杠杆比例
     */
    private BigDecimal proportion;
    /**
     * 爆仓比例
     */
    private BigDecimal explosionRiskRate=BigDecimal.ZERO;
    /**
     * 爆仓价
     */
    private BigDecimal explosionPrice =BigDecimal.ZERO;
}
