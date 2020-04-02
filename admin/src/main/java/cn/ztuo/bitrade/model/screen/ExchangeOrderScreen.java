package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.entity.ExchangeOrderDirection;
import cn.ztuo.bitrade.entity.ExchangeOrderStatus;
import cn.ztuo.bitrade.entity.ExchangeOrderType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExchangeOrderScreen extends AccountScreen{
    ExchangeOrderType type;
    String coinSymbol;//币单位
    String baseSymbol ;//结算单位
    String symbol ;//币对
    ExchangeOrderStatus status; //TRADING(交易中),COMPLETED（已完成）,CANCELED（已取消）,OVERTIMED（超时）;

    //成交价
    BigDecimal minPrice ;
    BigDecimal maxPrice ;
    //成交量
    BigDecimal minTradeAmount;
    BigDecimal maxTradeAmount;
    //成交额
    BigDecimal minTurnOver;
    BigDecimal maxTurnOver;
    String orderId ;
    ExchangeOrderDirection orderDirection ;

    /**
     * 01（委托订单  历史订单）
     */
    BooleanEnum completed;

    BooleanEnum marginTrade;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;
}
