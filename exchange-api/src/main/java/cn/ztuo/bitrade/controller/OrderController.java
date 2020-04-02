package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.MemberLevelEnum;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.CheckTraderOrderUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 委托订单处理类
 */
@Slf4j
@RestController
@RequestMapping("/order")
@Api(tags = "币币交易-委托")
public class OrderController {
    @Autowired
    private ExchangeOrderService orderService;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Value("${exchange.max-cancel-times:-1}")
    private int maxCancelTimes;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 添加委托订单
     *
     * @return
     */
    @RequestMapping(value = "add",method = RequestMethod.POST)
    @ApiOperation(value = "添加委托订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "direction", value = "BUY 0/SELL 1(交易方向)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "symbol", value = "交易对", required = true, dataType = "String"),
            @ApiImplicitParam(name = "price", value = "价格", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "amount", value = "数量", required = true, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "type", value = "交易类型", required = true, dataType = "String")
    })
    public MessageResult addOrder(
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember authMember,
            ExchangeOrderDirection direction,
            String symbol,
            BigDecimal price,
            BigDecimal amount,
            ExchangeOrderType type,
            BigDecimal triggerPrice) {
        log.info("用户下单authMember={},direction={},symbol={},price={},amount={},type={},triggerPrice={}"
                ,authMember,direction,symbol,price,amount,type,triggerPrice);
        ExchangeOrder order = null;
        if(direction == null || type == null){
            //TODO 提示信息不正确
            return MessageResult.error(500,msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        Member member=memberService.findOne(authMember.getId());
        /*if(member.getMemberLevel()== MemberLevelEnum.GENERAL){
            return MessageResult.error(500,msService.getMessage("NO_REAL_NAME"));
        }*/
        //是否被禁止交易
        if(member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)){
            return MessageResult.error(500,msService.getMessage("CANNOT_TRADE"));
        }
        order = new ExchangeOrder();
        if (price.compareTo(BigDecimal.ZERO) <= 0 && type != ExchangeOrderType.MARKET_PRICE ) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, msService.getMessage("NUMBER_OF_ILLEGAL"));
        }
        if( direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.CHECK_FULL_STOP){
            if(triggerPrice.compareTo(price)>=0){
                return MessageResult.error(500,msService.getMessage("BUY_CHECK_FULL_ILLEGAL"));
            }
        }
        if(direction == ExchangeOrderDirection.SELL && type==ExchangeOrderType.CHECK_FULL_STOP){
            if(triggerPrice.compareTo(price)<=0){
                return MessageResult.error(500,msService.getMessage("SELL_CHECK_FULL_ILLEGAL"));
            }
        }
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        if (exchangeCoin == null || exchangeCoin.getEnable() != 1) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }

        String baseCoin = exchangeCoin.getBaseSymbol();
        String exCoin = exchangeCoin.getCoinSymbol();
        Coin coin;
        if (direction == ExchangeOrderDirection.SELL) {
            coin = coinService.findByUnit(exCoin);
        } else {
            coin = coinService.findByUnit(baseCoin);
        }
        if (coin == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        //设置价格精度
        price = price.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
        //委托数量和精度控制
        if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
            amount = amount.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
            //最小成交额控制
            if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                return MessageResult.error(500, msService.getMessage("MIN_AMOUNT") + exchangeCoin.getMinTurnover());
            }
        } else {
            amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
            //成交量范围控制
            if(exchangeCoin.getMaxVolume()!=null&&exchangeCoin.getMaxVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMaxVolume().compareTo(amount)<0){
                return MessageResult.error(msService.getMessage("AMOUNT_OVER_SIZE")+" "+exchangeCoin.getMaxVolume());
            }
            if(exchangeCoin.getMinVolume()!=null&&exchangeCoin.getMinVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMinVolume().compareTo(amount)>0){
                return MessageResult.error(msService.getMessage("AMOUNT_TOO_SMALL")+" "+exchangeCoin.getMinVolume());
            }
        }
        if (amount.compareTo(BigDecimal.ZERO)<=0) {
            return MessageResult.error(msService.getMessage("AMOUNT_TOO_SMALL"));
        }
        MemberWallet baseCoinWallet = walletService.findByCoinUnitAndMemberId(baseCoin, member.getId());
        MemberWallet exCoinWallet = walletService.findByCoinUnitAndMemberId(exCoin, member.getId());
        if (baseCoinWallet == null || exCoinWallet == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        if (baseCoinWallet.getIsLock() == BooleanEnum.IS_TRUE || exCoinWallet.getIsLock() == BooleanEnum.IS_TRUE) {
            return MessageResult.error(500, msService.getMessage("WALLET_LOCKED"));
        }
        //如果有最低卖价限制，出价不能低于此价,且禁止市场价格卖
        if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) || type == ExchangeOrderType.MARKET_PRICE)) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        //查看是否启用市价买卖
        if (type == ExchangeOrderType.MARKET_PRICE) {
            if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.BUY) {
                return MessageResult.error(500, msService.getMessage("MARKET_BUY_NOT_SUPPORT"));
            } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ExchangeOrderDirection.SELL) {
                return MessageResult.error(500, msService.getMessage("MARKET_SELL_NOT_SUPPORT"));
            }
        }
        //限制委托数量
        if (exchangeCoin.getMaxTradingOrder() > 0 && orderService.findCurrentTradingCount(member.getId(), symbol, direction) >= exchangeCoin.getMaxTradingOrder()) {
            return MessageResult.error(500, msService.getMessage("MAX_TRADING_ORDER_OVER") + exchangeCoin.getMaxTradingOrder());
        }
        order.setMemberId(member.getId());
        order.setSymbol(symbol);
        order.setBaseSymbol(baseCoin);
        order.setCoinSymbol(exCoin);
        order.setType(type);
        order.setDirection(direction);
        if(order.getType() == ExchangeOrderType.MARKET_PRICE){
            order.setPrice(BigDecimal.ZERO);
        }
        else{
            order.setPrice(price);
            order.setTriggerPrice(triggerPrice);
        }
        order.setMarginTrade(BooleanEnum.IS_FALSE);
        //限价买入单时amount为用户设置的总成交额
        order.setAmount(amount);
        //用户下单
        order.setOrderResource(ExchangeOrderResource.CUSTOMER);
        MessageResult mr = orderService.addOrder(member.getId(), order);
        if (mr.getCode() != 0) {
            return MessageResult.error(500, msService.getMessage("ADD_ORDER_FAIL") + exchangeCoin.getMaxTradingOrder());
        }
        if(type==ExchangeOrderType.CHECK_FULL_STOP){
            //止盈止损单发送到其他地方
            kafkaTemplate.send("exchange-waiting-order",symbol,JSON.toJSONString(order));
        }else {
            //非止盈止损单直接发送到 发送消息至Exchange系统
            kafkaTemplate.send("exchange-order", symbol, JSON.toJSONString(order));
        }
        MessageResult result = MessageResult.success(msService.getMessage("SUCCESS"));
        result.setData(order.getOrderId());
        return result;
    }


    /**
     * 历史委托
     */
    @RequestMapping(value = "history",method = RequestMethod.POST)
    @ApiOperation(value = "查询历史委托")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer",defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer",defaultValue = "10")
    })
    @MultiDataSource(name = "second")
    public Page<ExchangeOrder> history(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol, int pageNo, int pageSize) {
        Page<ExchangeOrder> page = orderService.findHistory(member.getId(), symbol, pageNo, pageSize,BooleanEnum.IS_FALSE);
        ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
        page.getContent().forEach(exchangeOrder -> {
            exchangeOrder.setPriceStr(exchangeOrder.getPrice().setScale(coin.getBaseCoinScale(), RoundingMode.DOWN).toPlainString());
            exchangeOrder.setAmountStr(exchangeOrder.getAmount().setScale(coin.getCoinScale(),RoundingMode.DOWN).toPlainString());
            //获取交易成交详情
            exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
        });
        return page;
    }

    /**
     * 历史委托
     */
    @RequestMapping(value = "historyOrder",method = RequestMethod.POST)
    @ApiOperation(value = "查询历史成交")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer",defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer",defaultValue = "10")
    })
    @MultiDataSource(name = "second")
    public Page<ExchangeOrder> historyOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol, int pageNo, int pageSize) {
        Page<ExchangeOrder> page = orderService.findHistory(member.getId(), symbol, pageNo, pageSize,BooleanEnum.IS_FALSE);
        ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
        page.getContent().forEach(exchangeOrder -> {
            exchangeOrder.setPriceStr(exchangeOrder.getPrice().setScale(coin.getBaseCoinScale(), RoundingMode.DOWN).toPlainString());
            exchangeOrder.setAmountStr(exchangeOrder.getAmount().setScale(coin.getCoinScale(),RoundingMode.DOWN).toPlainString());
            //获取交易成交详情
            exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
        });
        return page;
    }


    /**
     * 当前委托
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "current",method = RequestMethod.POST)
    @ApiOperation(value = "查询当前委托")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer",defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer",defaultValue = "10")
    })
    @MultiDataSource(name = "second")
    public Page<ExchangeOrder> currentOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, String symbol, int pageNo, int pageSize) {
        Page<ExchangeOrder> page = orderService.findCurrent(member.getId(), symbol, pageNo, pageSize,BooleanEnum.IS_FALSE);
        ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
        page.getContent().forEach(exchangeOrder -> {
            exchangeOrder.setPriceStr(exchangeOrder.getPrice().setScale(coin.getBaseCoinScale(), RoundingMode.DOWN).toPlainString());
            exchangeOrder.setAmountStr(exchangeOrder.getAmount().setScale(coin.getCoinScale(),RoundingMode.DOWN).toPlainString());
            //获取交易成交详情
            BigDecimal tradedAmount = BigDecimal.ZERO;
            //获取成交总价
            BigDecimal turnover = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            exchangeOrder.setDetail(details);
            for (ExchangeOrderDetail trade : details) {
                tradedAmount = tradedAmount.add(trade.getAmount());
                turnover = turnover.add(trade.getTurnover());
            }
            exchangeOrder.setTradedAmount(tradedAmount);
            exchangeOrder.setTurnover(turnover);
        });
        return page;
    }


    /**
     * 查询委托成交明细
     *
     * @param member
     * @param orderId
     * @return
     */
    @RequestMapping(value = "detail/{orderId}",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "查询委托成交明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "订单id", required = true, dataType = "String")
    })
    public List<ExchangeOrderDetail> currentOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) {
        return exchangeOrderDetailService.findAllByOrderId(orderId);
    }
    @ApiOperation(value = "撤销委托的交易")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "订单id", required = true, dataType = "String")
    })
    @RequestMapping("cancel/{orderId}")
    public MessageResult cancelOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String orderId) throws Exception {
        ExchangeOrder order = orderService.findOne(orderId);
        if (order.getMemberId() != member.getId()) {
            return MessageResult.error(500, msService.getMessage("NO_CANCEL"));
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING && order.getStatus() != ExchangeOrderStatus.WAITING_TRIGGER) {
            return MessageResult.error(500, msService.getMessage("NOT_TRADING_ORDER"));
        }
        if (maxCancelTimes > 0 && orderService.findTodayOrderCancelTimes(member.getId(), order.getSymbol()) >= maxCancelTimes) {
            return MessageResult.error(500, msService.getMessage("MAX_CANCEL_TIMES_OVER")+ maxCancelTimes);
        }
        if(order.getType()==ExchangeOrderType.CHECK_FULL_STOP){
            //止盈止损取消
            if(order.getStatus() == ExchangeOrderStatus.WAITING_TRIGGER && CheckTraderOrderUtil.isWaitingOrderExist(order,restTemplate)){
                kafkaTemplate.send("exchange-waiting-order-cancel",order.getSymbol(), JSON.toJSONString(order));
            } else if(System.currentTimeMillis() - order.getTime() > 3600 * 1000) {
                orderService.forceCancelOrder(order);
            }
            else {
                return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }else {
            if (order.getStatus() == ExchangeOrderStatus.TRADING && CheckTraderOrderUtil.isExchangeOrderExist(order, restTemplate)) {
                // 发送消息至Exchange系统
                kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));
            } else if(System.currentTimeMillis() - order.getTime() > 3600 * 1000) {
                // 强制取消
                orderService.forceCancelOrder(order);
            }
            else {
                return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }
        return MessageResult.success(msService.getMessage("SUCCESS"));
    }

    /**
     * 个人中心历史委托
     */
    @RequestMapping("personal/history")
    @ApiOperation(value = "个人中心历史委托")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对", required = false, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "交易类型", required = false, dataType = "String"),
            @ApiImplicitParam(name = "status", value = "订单状态", required = false, dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "direction", value = "BUY/SELL(交易方向)", required = false, dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = false, dataType = "Integer",defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = false, dataType = "Integer",defaultValue = "10")
    })
    public Page<ExchangeOrder> personalHistoryOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                    String symbol,ExchangeOrderType type,ExchangeOrderStatus status,String startTime,
                                                    String endTime,ExchangeOrderDirection direction,int pageNo,int pageSize) {

        Page<ExchangeOrder> page = orderService.findPersonalHistory(member.getId(), symbol, type, status, startTime, endTime,direction, pageNo, pageSize);
        page.getContent().forEach(exchangeOrder -> {
            //获取交易成交详情
            exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
        });
        return page;
    }


    /**
     * 个人中心当前委托
     * @param member
     * @param symbol
     * @param type
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping("personal/current")
    @ApiOperation(value = "个人中心当前委托")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "交易对", required = false, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "交易类型", required = false, dataType = "String"),
            @ApiImplicitParam(name = "status", value = "订单状态", required = false, dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "direction", value = "BUY/SELL(交易方向)", required = false, dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = false, dataType = "Integer",defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = false, dataType = "Integer",defaultValue = "10")
    })
    public Page<ExchangeOrder> personalCurrentOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                    String symbol,ExchangeOrderType type,ExchangeOrderStatus status,String startTime,
                                                    String endTime,ExchangeOrderDirection direction,int pageNo,int pageSize){
        Page<ExchangeOrder> page = orderService.findPersonalCurrent(member.getId(), symbol,type,startTime,endTime, direction, pageNo, pageSize);
        page.getContent().forEach(exchangeOrder -> {
            //获取交易成交详情
            BigDecimal tradedAmount = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            exchangeOrder.setDetail(details);
            for (ExchangeOrderDetail trade : details) {
                tradedAmount = tradedAmount.add(trade.getAmount());
            }
            exchangeOrder.setTradedAmount(tradedAmount);
        });
        return page;
    }


}
