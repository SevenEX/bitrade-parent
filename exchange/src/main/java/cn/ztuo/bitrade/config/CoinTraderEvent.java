package cn.ztuo.bitrade.config;

import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.Trader.CoinTrader;
import cn.ztuo.bitrade.Trader.CoinTraderFactory;
import cn.ztuo.bitrade.entity.ExchangeOrder;
import cn.ztuo.bitrade.entity.ExchangeOrderDetail;
import cn.ztuo.bitrade.service.ExchangeOrderDetailService;
import cn.ztuo.bitrade.service.ExchangeOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class CoinTraderEvent implements ApplicationListener<ContextRefreshedEvent> {
    private Logger log = LoggerFactory.getLogger(CoinTraderEvent.class);
    @Autowired
    CoinTraderFactory coinTraderFactory;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("======initialize coinTrader======");
        HashMap<String,CoinTrader> traders = coinTraderFactory.getTraderMap();
        traders.forEach((symbol,trader) ->{
            List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
            List<ExchangeOrder> tradingOrders = new ArrayList<>();
            List<ExchangeOrder> completedOrders = new ArrayList<>();
            orders.forEach(order -> {
                BigDecimal tradedAmount = BigDecimal.ZERO;
                BigDecimal turnover = BigDecimal.ZERO;
                List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());
                //order.setDetail(details);
                for(ExchangeOrderDetail trade:details){
                    tradedAmount = tradedAmount.add(trade.getAmount());
                    turnover = turnover.add(trade.getAmount().multiply(trade.getPrice()));
                }
                order.setTradedAmount(tradedAmount);
                order.setTurnover(turnover);
                if(!order.isCompleted()){
                    tradingOrders.add(order);
                }
                else{
                    completedOrders.add(order);
                }
            });
            log.info("trader={}",trader);
            trader.trade(tradingOrders);
            //判断已完成的订单发送消息通知
            if(completedOrders.size() > 0){
                kafkaTemplate.send("exchange-order-completed", symbol, JSON.toJSONString(completedOrders));
            }
            trader.setReady(true);
        });
    }

}
