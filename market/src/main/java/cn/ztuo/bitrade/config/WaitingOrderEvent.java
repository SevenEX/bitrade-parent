package cn.ztuo.bitrade.config;

import cn.ztuo.bitrade.entity.ExchangeOrder;
import cn.ztuo.bitrade.entity.ExchangeOrderDirection;
import cn.ztuo.bitrade.entity.ExchangeOrderStatus;
import cn.ztuo.bitrade.service.ExchangeOrderService;
import cn.ztuo.bitrade.waiting.WaitingOrder;
import cn.ztuo.bitrade.waiting.WaitingOrderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @description: WaitingOrderEvent
 * @author: MrGao
 * @create: 2019/04/27 15:33
 */
@Slf4j
@Component
public class WaitingOrderEvent implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private WaitingOrderFactory waitingOrderFactory;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("======initialize waitingOrder======");
        HashMap<String,WaitingOrder> waitingOrderMap = waitingOrderFactory.getWaitingOrderHashMap();
        waitingOrderMap.forEach((symbol,waitingOrder) -> {
            //查询所有等待的交易的止损止盈挂单
            List<ExchangeOrder> orders = exchangeOrderService.findAllWaitingOrder(symbol,ExchangeOrderStatus.WAITING_TRIGGER);
            log.info("待挂单数量={}",orders.size());
            //将订单按照买卖分组
            Map<ExchangeOrderDirection, List<ExchangeOrder>> ordersMap = new HashMap<>();
            orders.forEach(order -> {
                List<ExchangeOrder> tempList = ordersMap.get(order.getDirection());
                if (tempList == null) {
                    tempList = new ArrayList<>();
                    tempList.add(order);
                    ordersMap.put(order.getDirection(), tempList);
                } else {
                    tempList.add(order);
                }
            });
            List<ExchangeOrder> buyOrders = ordersMap.get(ExchangeOrderDirection.BUY);
            if(buyOrders!=null && buyOrders.size()>0){
                //买单按照价格充小到大
                Collections.sort(buyOrders, Comparator.comparing(ExchangeOrder::getTriggerPrice));
                waitingOrder.dealWaitingOrder(buyOrders);
            }
            List<ExchangeOrder> sellOrders = ordersMap.get(ExchangeOrderDirection.SELL);
            if(sellOrders!=null && sellOrders.size()>0){
                //买单按照价格充大到小
                Collections.sort(sellOrders, Comparator.comparing(ExchangeOrder::getTriggerPrice).reversed());
                waitingOrder.dealWaitingOrder(sellOrders);
            }
            log.info("初始化完毕waitingOrder={}",waitingOrder);
            waitingOrder.setReady(true);
        });
    }
}
