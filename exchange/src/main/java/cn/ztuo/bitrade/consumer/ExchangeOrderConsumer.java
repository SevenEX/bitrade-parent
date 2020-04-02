package cn.ztuo.bitrade.consumer;

import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.Trader.CoinTrader;
import cn.ztuo.bitrade.Trader.CoinTraderFactory;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.entity.ExchangeOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ExchangeOrderConsumer {
    @Autowired
    private CoinTraderFactory traderFactory;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;


    @KafkaListener(topics = "exchange-symbol",containerFactory = "kafkaListenerContainerFactory")
    public void onAddCoinTradeByExchangeCoin(List<ConsumerRecord<String,String>> records){
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String,String> record  = records.get(i);
            log.info("接收exchange-product>>topic={},value={},size={}",record.topic(),record.value(),records.size());
            List<ExchangeCoin> exchangeCoins = JSON.parseArray(record.value(), ExchangeCoin.class);
            exchangeCoins.forEach(exchangeCoin -> {
                CoinTrader coinTrader = traderFactory.getTrader(exchangeCoin.getSymbol());
                if(coinTrader==null){
                    coinTrader = new CoinTrader(exchangeCoin.getSymbol());
                    coinTrader.setKafkaTemplate(kafkaTemplate);
                    coinTrader.setBaseCoinScale(exchangeCoin.getBaseCoinScale());
                    coinTrader.setCoinScale(exchangeCoin.getCoinScale());
                    coinTrader.initialize();
                    coinTrader.setReady(true);
                    traderFactory.addTrader(exchangeCoin.getSymbol(),coinTrader);
                    log.info("添加完毕");
                }
            });
        }
    }

    @KafkaListener(topics = "exchange-order",containerFactory = "kafkaListenerContainerFactory")
    public void onOrderSubmitted(List<ConsumerRecord<String,String>> records){
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String,String> record = records.get(i);
            log.info("onOrderSubmitted:topic={},key={}", record.topic(), record.key());
            String symbol = record.key();
            ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
            if (order == null) {
                return;
            }
            CoinTrader trader = traderFactory.getTrader(symbol);
            //如果当前币种交易暂停会自动取消订单
            if (trader.isTradingHalt() || !trader.getReady()) {
                //撮合器未准备完成，撤回当前等待的订单
                kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
            } else {
                try {
                    long startTick = System.currentTimeMillis();
                    trader.trade(order);
                    log.info("complete trade,{}ms used!", System.currentTimeMillis() - startTick);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("====交易出错，退回订单===");
                    kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
                }
            }
        }
    }

    @KafkaListener(topics = "exchange-order-cancel",containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCancel(List<ConsumerRecord<String,String>> records){
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            log.info("onOrderCancel:topic={},accessKey={}", record.topic(), record.key());
            String symbol = record.key();
            ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
            if (order == null) {
                return;
            }
            CoinTrader trader = traderFactory.getTrader(symbol);
            if(trader == null) {
                kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(order));
                return;
            }
            if (trader.getReady()) {
                try {
                    ExchangeOrder result = trader.cancelOrder(order);
                    if (result != null) {
                        kafkaTemplate.send("exchange-order-cancel-success", order.getSymbol(), JSON.toJSONString(result));
                    }
                } catch (Exception e) {
                    log.error("====取消订单出错===e={}",e);
                }
            }
        }
    }
}
