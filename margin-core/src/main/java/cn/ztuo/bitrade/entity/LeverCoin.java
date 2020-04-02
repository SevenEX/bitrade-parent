package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 杠杆币对
 * @author zhang yingxin
 * @date 2018/5/26
 */
@Entity
@Data
@Table
public class LeverCoin {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    //币对
    @NotNull
    private String symbol;
    //交易币种符号
    @NotNull
    private String coinSymbol;
    //结算币种符号，如USDT
    @NotNull
    private String baseSymbol;
    //状态，1：启用，0：禁止
    private BooleanEnum enable=BooleanEnum.IS_TRUE;
    //排序，从小到大
    private int sort;
    //杠杆比例
    @NotNull
    private BigDecimal proportion;

    @Column(columnDefinition = "decimal(20,8) comment '借贷利率'")
    private BigDecimal interestRate=BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(20,8) comment '最小转入金额'")
    private BigDecimal minTurnIntoAmount=BigDecimal.ZERO;

    @Column(columnDefinition = "decimal(20,8) comment '最小转出金额'")
    private BigDecimal minTurnOutAmount=BigDecimal.ZERO;

    //private BigDecimal riskRate;//风险率
}
