package cn.ztuo.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.TransactionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberTransactionScreen extends AccountScreen{

    /**
     * 交易时间搜索
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;
    /**
     *  RECHARGE("充值"),
     *  WITHDRAW("提现"),
     *  TRANSFER_ACCOUNTS("转账"),
     *  EXCHANGE("币币交易"),
     *  OTC_BUY("法币买入"),
     *  OTC_SELL("法币卖出"),
     *  ACTIVITY_AWARD("活动奖励"),
     *  PROMOTION_AWARD("推广奖励"),
     *  DIVIDEND("分红"),
     *  VOTE("投票"),
     *   ADMIN_RECHARGE("人工充值"),
     *  MATCH("配对");
     *
     */
    private TransactionType type;

    /**
     * 交易金额搜索
     */
    private BigDecimal minMoney ;
    private BigDecimal maxMoney ;

    /**
     * 手续费搜索
     */
    private BigDecimal minFee ;
    private BigDecimal maxFee ;

    private String symbol;

    private Long airdropId;

}
