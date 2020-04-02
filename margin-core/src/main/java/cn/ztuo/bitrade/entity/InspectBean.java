package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Embeddable
public class InspectBean {
    private Long memberId;
    private String memberName;
    private Member member;
    private LeverCoin leverCoin;
    private List<LoanRecord> loanRecordList;
    private List<LeverWallet> leverWalletList;
    /**
     * 风险率
     */
    private BigDecimal riskRate;
    /**
     * 总资产 USDT
     */
    private BigDecimal totalAmount;
    /**
     * 总借贷 USDT
     */
    private BigDecimal totalLoan;

}
