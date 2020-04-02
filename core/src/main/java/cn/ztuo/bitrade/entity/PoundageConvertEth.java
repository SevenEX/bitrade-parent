package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 手续费折合以太坊信息表
 */
@Entity
@Data
public class PoundageConvertEth {

    //主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id ;
    //关联订单表 一条订单 多条挖矿交易记录
    private String exchangeOrderId ;
    //会员Id
    private Long memberId ;
    //挖币数量
    @Column(columnDefinition = "decimal(20,8) DEFAULT 0 ")
    private BigDecimal mineAmount = BigDecimal.ZERO;
    //手续费
    @Column(columnDefinition = "decimal(20,8) DEFAULT 0 ")
    private BigDecimal poundageAmount = BigDecimal.ZERO;
    //折合以太坊手续费
    @Column(columnDefinition = "decimal(20,8) DEFAULT 0 ")
    private BigDecimal poundageAmountEth = BigDecimal.ZERO;

    //手续费币种id
    private String coinId ;
    //交易时间
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date transactionTime;

    //交易对符号
    private String  symbol ;
    //挂单类型  0市价 1限价
    private String  type ;
    //方向 0 买 1卖
    private String  direction ;
    //USDT费力
    private String usdtRate ;
    //ETH2USDTRate
    private String ethUsdtRate ;



}
