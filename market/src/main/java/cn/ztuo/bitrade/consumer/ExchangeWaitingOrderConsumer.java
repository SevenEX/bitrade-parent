package cn.ztuo.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.entity.ExchangeOrder;
import cn.ztuo.bitrade.entity.ExchangeTrade;
import cn.ztuo.bitrade.processor.CoinProcessor;
import cn.ztuo.bitrade.waiting.WaitingOrder;
import cn.ztuo.bitrade.waiting.WaitingOrderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: ExchangeWattingOrderConsumer
 * @author: MrGao
 * @create: 2019/04/27 15:03
 */
@Slf4j
@Component
public class ExchangeWaitingOrderConsumer {

    @Autowired
    private WaitingOrderFactory waitingOrderFactory ;


    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    /**
     * 处理止盈止损订单
     * @param records
     */
    @KafkaListener(topics = "exchange-waiting-order", containerFactory = "kafkaListenerContainerFactory")
    public void handleTrade(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            log.info("exchange-waiting-order topic={},key={},value={}", record.topic(), record.key(), record.value());
            long startTick = System.currentTimeMillis();
            String symbol = record.key();
            ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
            WaitingOrder waitingOrder = waitingOrderFactory.getWaitingOrder(symbol);
            if (waitingOrder.isReady()) {
                waitingOrder.dealOrder(order);
            } else {
                //取消订单
                kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
            }
            log.info("complete waiting process,{}ms used!", System.currentTimeMillis() - startTick);
        }
    }

    /**
     * 处理止盈止损订单
     * @param records
     */
    @KafkaListener(topics = "exchange-waiting-order-cancel", containerFactory = "kafkaListenerContainerFactory")
    public void cancelWaitingOrder(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            log.info("exchange-waiting-order-cancel topic={},key={},value={}", record.topic(), record.key(), record.value());
            long startTick = System.currentTimeMillis();
            String symbol = record.key();
            ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
            WaitingOrder waitingOrder = waitingOrderFactory.getWaitingOrder(symbol);
            if (waitingOrder.isReady()) {
                try {
                    ExchangeOrder cancelResult = waitingOrder.cancel(order);
                    if(cancelResult!=null){
                        kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
                    }
                } catch (Exception e) {
                    log.info("取消止盈止损订单异常={}",e);
                }
            }
            log.info("exchange-waiting-order-cancel,{}ms used!", System.currentTimeMillis() - startTick);
        }
    }
}
