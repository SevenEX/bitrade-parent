package cn.ztuo.bitrade.job;

import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.entity.ExchangeOrder;
import cn.ztuo.bitrade.entity.ExchangeOrderDirection;
import cn.ztuo.bitrade.entity.ExchangeTrade;
import cn.ztuo.bitrade.entity.MergeOrder;
import cn.ztuo.bitrade.processor.CoinProcessor;
import cn.ztuo.bitrade.processor.CoinProcessorFactory;
import cn.ztuo.bitrade.service.ExchangeOrderService;
import cn.ztuo.bitrade.waiting.WaitingOrder;
import cn.ztuo.bitrade.waiting.WaitingOrderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @description: WaitingOrderPushJob
 * @author: MrGao
 * @create: 2019/04/27 17:00
 */
@Slf4j
@Component
public class WaitingOrderPushJob {

    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private WaitingOrderFactory factory ;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    private ExchangeOrderService orderService ;

    @Scheduled(fixedRate = 500)
    public void pushTrade(){
        HashMap<String, CoinProcessor> coinProcessorMap = coinProcessorFactory.getProcessorMap();
        coinProcessorMap.forEach((symbol,coinProcessor)->{
            WaitingOrder waitingOrder = factory.getWaitingOrder(symbol);
//            log.info("处理symbol={}",symbol);
            TreeMap<BigDecimal,MergeOrder> buyMap = waitingOrder.getBuyTriggerPriceQueue();
            TreeMap<BigDecimal,MergeOrder> sellMap = waitingOrder.getSellTriggerPriceQueue();
            Iterator<Map.Entry<BigDecimal,MergeOrder>> buyIterator = buyMap.entrySet().iterator();
            while (buyIterator.hasNext()){
                Map.Entry<BigDecimal,MergeOrder> entry = buyIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                if(coinProcessor.getThumb()==null){
                    log.info("thumb未初始化");
                    return;
                }
                BigDecimal currentPrice = coinProcessor.getThumb().getClose();
                if(mergeOrder.size()==0){
                    return;
                }
//                log.info("mergeOrderPrice={},currentPrice={}",mergeOrder.getPrice(),currentPrice);
                if(currentPrice.compareTo(mergeOrder.getPrice()) > 0){
                    Iterator<ExchangeOrder> orderIterator =mergeOrder.iterator();
                    int count = 0 ;
                    while (orderIterator.hasNext()){
                        ExchangeOrder order = orderIterator.next();
                        //改变订单状态，将订单改为已挂单
                        int i = orderService.pushWaitingOrderByOrderId(order.getOrderId());
                        log.info("达到触发价i={}==将该价格下所有订单挂出currentPrice={},getPrice={}",i,currentPrice,mergeOrder.getPrice());
                        if(i>0) {
                            count++ ;
                            kafkaTemplate.send("exchange-order", order.getSymbol(), JSON.toJSONString(order));
                            log.info("发送订单成功={}",order);
                            orderIterator.remove();
                        }
                    }
                    if(mergeOrder.size()==count) {
                        log.info("移除key={}",count);
                        buyMap.remove(entry.getKey());
                    }
                }
            }
            Iterator<Map.Entry<BigDecimal,MergeOrder>> sellIterator = sellMap.entrySet().iterator();
            while (sellIterator.hasNext()){
                Map.Entry<BigDecimal,MergeOrder> entry = sellIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                if(coinProcessor.getThumb()==null){
                    log.info("thumb未初始化");
                    return;
                }
                BigDecimal currentPrice = coinProcessor.getThumb().getClose();
                if(mergeOrder.size()==0){
                    return;
                }
//                log.info("mergeOrderPrice={},currentPrice={}",mergeOrder.getPrice(),currentPrice);
                if(currentPrice.compareTo(mergeOrder.getPrice())==-1){
                    Iterator<ExchangeOrder> orderIterator =mergeOrder.iterator();
                    int count = 0 ;
                    while (orderIterator.hasNext()){
                        log.info("达到触发价==将该价格下所有订单挂出currentPrice={},getPrice={}",currentPrice,mergeOrder.getPrice());
                        ExchangeOrder order = orderIterator.next();
                        int i = orderService.pushWaitingOrderByOrderId(order.getOrderId());
                        if(i>0) {
                            count++ ;
                            kafkaTemplate.send("exchange-order", order.getSymbol(), JSON.toJSONString(order));
                            orderIterator.remove();
                            log.info("发送订单成功={}",order);
                        }
                    }
                    if(mergeOrder.size()==count) {
                        buyMap.remove(entry.getKey());
                        log.info("移除key={}",count);
                    }
                    sellMap.remove(entry.getKey());
                }
            }
        });
    }

}
