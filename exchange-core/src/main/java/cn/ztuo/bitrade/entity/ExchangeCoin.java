package cn.ztuo.bitrade.entity;


import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Data
public class ExchangeCoin {
    //交易币种名称，格式：BTC/USDT
    @NotNull(message = "{ExchangeCoin.id.null}")
    @Id
    private String symbol;
    //交易币种符号
    private String coinSymbol;
    //结算币种符号，如USDT
    private String baseSymbol;
    //状态，1：启用，2：禁止
    private int enable;
    //交易手续费
    @Column(columnDefinition = "decimal(20,8) default 0 comment '基币手续费'")
    private BigDecimal fee;
    @Column(columnDefinition = "decimal(20,8) default 0 comment '结算币种手续费'")
    private BigDecimal baseFee;
    //排序，从小到大
    private int sort;
    //数量精度
    private int coinScale;
    //价格精度
    private int baseCoinScale;
    @Column(columnDefinition = "decimal(20,8) default 0 comment '卖单最低价格'")
    private BigDecimal minSellPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) default 1 comment '是否启用市价卖'")
    private BooleanEnum  enableMarketSell = BooleanEnum.IS_TRUE;

    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int(11) default 1 comment '是否启用市价买'")
    private BooleanEnum  enableMarketBuy = BooleanEnum.IS_TRUE;

    /**
     * 最大交易时间
     */
    @Column(columnDefinition = "int(11) default 0 comment '委托超时自动下架时间，单位为秒，0表示不过期'")
    private int maxTradingTime = 0 ;

    /**
     * 最大在交易中的委托数量
     */
    @Column(columnDefinition = "int(11) default 0 comment '最大允许同时交易的订单数，0表示不限制'")
    private int maxTradingOrder = 0;

    /**
     * 标签位，用于推荐，排序等,默认为0，1表示推荐，
     */
    @Column(columnDefinition = "int(11) default 0 ")
    private int flag = 0 ;

    @Column(columnDefinition = "decimal(20,8) default 0 comment '最小成交额'")
    private BigDecimal minTurnover = BigDecimal.ZERO;

    /**
     * 交易区域
     */
    @Column(columnDefinition = "int(11) default 0 ")
    private int zone = 0;

    /**
     * 最小下单量，0表示不限制
     */
    @Column(columnDefinition = "decimal(20,8) default 0 comment '最小下单量'")
    private BigDecimal minVolume =BigDecimal.ZERO;
    /**
     * 最大下单量，0表示不限制
     */
    @Column(columnDefinition = "decimal(20,8) default 0 comment '最大下单量'")
    private BigDecimal maxVolume =BigDecimal.ZERO;

    /**
     * 默认交易对儿，0-非默认，1-默认，2-币种内默认
     */
    private String defaultSymbol = "0";

    //新币榜排序（倒序展示，小于0或为空则不展示）
    private int newSort;

    private int areaId;
}
