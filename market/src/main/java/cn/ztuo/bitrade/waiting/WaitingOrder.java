package cn.ztuo.bitrade.waiting;

import cn.ztuo.bitrade.entity.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

/**
 * @description: WaitingOrder
 * @author: MrGao
 * @create: 2019/04/27 15:20
 */
@Slf4j
@Data
public class WaitingOrder {

    private String symbol;
    /**
     *止盈止损买入 按照触发价从小到大排序 小价格在前 大价格在后
     */
    private TreeMap<BigDecimal,MergeOrder> buyTriggerPriceQueue;
    /**
     * 卖出限价订单链表，价格从低到高排列
     */
    private TreeMap<BigDecimal,MergeOrder> sellTriggerPriceQueue;

    /**
     * 是否初始化完毕
     */
    private boolean ready = false;

    public WaitingOrder(String symbol){
        this.symbol=symbol;
    }

    /**
     * 初始化交易线程
     */
    public void initialize(){
        log.info("init waiting for symbol {}",symbol);
        //买单队列价格降序排列
        buyTriggerPriceQueue = new TreeMap<>(Comparator.naturalOrder());
        //卖单队列价格升序排列
        sellTriggerPriceQueue = new TreeMap<>(Comparator.reverseOrder());
    }


    /**
     * 增加限价订单到队列，买入单按从价格高到低排，卖出单按价格从低到高排
     * @param exchangeOrder
     */
    public void addWaitingOrder(ExchangeOrder exchangeOrder){
        if(exchangeOrder.getType() != ExchangeOrderType.CHECK_FULL_STOP){
            return ;
        }
        log.info("addWaitingOrder,orderId = {}", exchangeOrder.getOrderId());
        TreeMap<BigDecimal,MergeOrder> list;
        String name  ;
        if(exchangeOrder.getDirection() == ExchangeOrderDirection.BUY){
            list = buyTriggerPriceQueue;
            name="buyTriggerPriceQueue";
        }else {
            list = sellTriggerPriceQueue;
            name="sellTriggerPriceQueue";
        }
        synchronized (list) {
            MergeOrder mergeOrder = list.get(exchangeOrder.getTriggerPrice());
            if(mergeOrder == null){
                mergeOrder = new MergeOrder();
                mergeOrder.add(exchangeOrder);
                list.put(exchangeOrder.getTriggerPrice(),mergeOrder);
                log.info(name+"中无此价格新增价格梯度={}",list);
            }else {
                mergeOrder.add(exchangeOrder);
                log.info(name+"中有此价格新增价格梯度={}",list);
            }
        }
    }

    public void dealWaitingOrder(List<ExchangeOrder> exchangeOrders){
        for (ExchangeOrder order :exchangeOrders){
            dealOrder(order);
        }

    }

    public void dealOrder(ExchangeOrder order) {
        if(order.getType() != ExchangeOrderType.CHECK_FULL_STOP){
            return ;
        }
        log.info("waiting order={}",order);
        if(!symbol.equalsIgnoreCase(order.getSymbol())){
            log.info("unsupported symbol={},coin={},base={}",order.getSymbol(), order.getCoinSymbol(), order.getBaseSymbol());
            return ;
        }
        if(order.getAmount().compareTo(BigDecimal.ZERO) <=0 || order.getAmount().subtract(order.getTradedAmount()).compareTo(BigDecimal.ZERO)<=0){
            return ;
        }
        addWaitingOrder(order);
    }

    public ExchangeOrder findWaitingOrder(String orderId, ExchangeOrderDirection direction) {
        TreeMap<BigDecimal,MergeOrder> list;
        if(direction==ExchangeOrderDirection.SELL){
            list = sellTriggerPriceQueue;
        }else {
            list = buyTriggerPriceQueue;
        }
        synchronized (list) {
            Iterator<Map.Entry<BigDecimal,MergeOrder>> mergeOrderIterator = list.entrySet().iterator();
            while (mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal,MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                while ((orderIterator.hasNext())) {
                    ExchangeOrder order = orderIterator.next();
                    if (order.getOrderId().equalsIgnoreCase(orderId)) {
                        log.info("堆栈中有该信息");
                        return order;
                    }
                }
            }
        }
        return null;
    }

    public ExchangeOrder cancel(ExchangeOrder exchangeOrder) {
        log.info("取消止盈止损队列order={}",exchangeOrder);
        if(exchangeOrder.getType()!=ExchangeOrderType.CHECK_FULL_STOP){
            log.info("类型不对");
            return null;
        }
        TreeMap<BigDecimal,MergeOrder> list;
        if(exchangeOrder.getDirection()==ExchangeOrderDirection.BUY){
            list = buyTriggerPriceQueue;
        }else {
            list = sellTriggerPriceQueue;
        }
        synchronized (list) {
            MergeOrder mergeOrder = list.get(exchangeOrder.getTriggerPrice());
            Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
            if (orderIterator != null) {
                while (orderIterator.hasNext()) {
                    ExchangeOrder order = orderIterator.next();
                    if (order.getOrderId().equalsIgnoreCase(exchangeOrder.getOrderId())) {
                        orderIterator.remove();
                        if (mergeOrder.size() == 0) {
                            list.remove(exchangeOrder.getPrice());
                        }
                        log.info("移除该订单+{}",order.getOrderId());
                        return order;
                    }
                }
            }
        }
        log.info("队列没有");
        return null;
    }
}
