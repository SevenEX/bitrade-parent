package cn.ztuo.bitrade.consumer;


import cn.ztuo.bitrade.component.CoinExchangeRate;
import cn.ztuo.bitrade.constant.NettyCommand;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.handler.MongoMarketHandler;
import cn.ztuo.bitrade.handler.NettyHandler;
import cn.ztuo.bitrade.handler.WebsocketMarketHandler;
import cn.ztuo.bitrade.job.ExchangePushJob;
import cn.ztuo.bitrade.processor.CoinProcessor;
import cn.ztuo.bitrade.processor.CoinProcessorFactory;
import cn.ztuo.bitrade.processor.DefaultCoinProcessor;
import cn.ztuo.bitrade.service.DataDictionaryService;
import cn.ztuo.bitrade.service.ExchangeOrderService;
import cn.ztuo.bitrade.service.MarketService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.UUIDUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
@Slf4j
public class ExchangeTradeConsumer {
    private Logger logger = LoggerFactory.getLogger(ExchangeTradeConsumer.class);
    @Autowired
    private DataDictionaryService dataDictionaryService;
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private CoinExchangeRate coinExchangeRate;
    @Autowired
    private NettyHandler nettyHandler;
    @Value("${second.referrer.award}")
    private boolean secondReferrerAward;
    private ExecutorService executor = Executors.newFixedThreadPool(30);
    @Autowired
    private ExchangePushJob pushJob;

    @Autowired
    private MongoMarketHandler mongoMarketHandler;
    @Autowired
    private WebsocketMarketHandler wsHandler;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private MarketService marketService;
    @Autowired
    private CoinExchangeRate exchangeRate;

    private String uuid;

    @PostConstruct
    public void init() {
        uuid = UUIDUtil.getUUID();
    }

    @KafkaListener(topics = "exchange-market-symbol",containerFactory = "kafkaListenerContainerFactory")
    public void onAddCoinTradeByExchangeCoin(List<ConsumerRecord<String,String>> records){
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String,String> record  = records.get(i);
            log.info("接收exchange-market-product>>topic={},value={},size={}",record.topic(),record.value(),records.size());
            List<ExchangeCoin> exchangeCoins = JSON.parseArray(record.value(), ExchangeCoin.class);
            exchangeCoins.forEach(exchangeCoin -> {
                CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(exchangeCoin.getSymbol());
                if(coinProcessor==null){
                    CoinProcessor processor = new DefaultCoinProcessor(exchangeCoin.getSymbol(), exchangeCoin.getBaseSymbol());
                    processor.addHandler(mongoMarketHandler);
                    processor.addHandler(wsHandler);
                    processor.addHandler(nettyHandler);
                    processor.setMarketService(marketService);
                    processor.setExchangeRate(exchangeRate);
                    processor.initializeThumb();
                    processor.initializeUsdRate();
                    processor.setIsHalt(false);
                    coinProcessorFactory.addProcessor(exchangeCoin.getSymbol(), processor);
                }
            });
            exchangeRate.setCoinProcessorFactory(coinProcessorFactory);
        }
    }

    /**
     * 处理成交明细
     *
     * @param records
     */
    @KafkaListener(topics = "exchange-trade", containerFactory = "kafkaListenerContainerFactory")
    public void handleTrade(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            logger.info("exchange-trade topic={},key={},value={}", record.topic(), record.key(), record.value());
            long startTick = System.currentTimeMillis();
            String symbol = record.key();
            List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
            trades.forEach(trade->trade.setSenderUuid(getUuid()));
            //处理K线行情
            kafkaTemplate.send("exchange-trade-kline", symbol, JSON.toJSONString(trades));
            executor.submit(new HandleTradeThread(record));
            log.info("complete exchange process,{}ms used!", System.currentTimeMillis() - startTick);
        }
    }

    @KafkaListener(topics = "exchange-trade-kline", containerFactory = "kafkaListenerContainerFactory2")
    public void handleTradeKline(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            String symbol = record.key();
            CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
            List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
            pushJob.addTrades(symbol, trades);
            if (coinProcessor != null) {
                coinProcessor.process(trades);
            }
        }
    }

    /**
     * 订单完成
     * @param records
     */
    @KafkaListener(topics = "exchange-order-completed",  containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCompleted(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            logger.info("exchange-order-completed topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
            String symbol = record.key();
            try {
                List<ExchangeOrder> orders = JSON.parseArray(record.value(), ExchangeOrder.class);
                for (ExchangeOrder order : orders) {
                    //委托成交完成处理
                    exchangeOrderService.orderCompleted(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
                    //推送订单成交
                    messagingTemplate.convertAndSend("/topic/market/order-completed/" + symbol + "/" + order.getMemberId(), order);
                    nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_COMPLETED, order);
                }
            } catch (Exception e) {
                logger.info(" 订单完成ERROR={}", e);
            }
        }
    }

    /**
     * 处理模拟交易
     *
     * @param records
     */
    @KafkaListener(topics = "exchange-trade-mocker", containerFactory = "kafkaListenerContainerFactory")
    public void handleMockerTrade(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            logger.info("exchange-trade-mocker topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
            try {
                List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
                String symbol = record.key();
                //处理行情
                CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
                if (coinProcessor != null) {
                    coinProcessor.process(trades);
                }
                pushJob.addTrades(symbol, trades);
            } catch (Exception e) {
                logger.info(" 处理模拟交易ERROR={}", e);
            }
        }
    }

    /**
     * 消费交易盘口信息
     *
     * @param records
     */
    @KafkaListener(topics = "exchange-trade-plate",containerFactory = "kafkaListenerContainerFactory", properties = {"auto.offset.reset=latest"})
    public void handleTradePlate(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            try {
                logger.info("exchange-trade-plate topic={},accessKey={}", record.topic(), record.key());
                String symbol = record.key();
                TradePlate plate = JSON.parseObject(record.value(), TradePlate.class);
                pushJob.addPlates(symbol, plate);
            } catch (Exception e) {
                logger.info("消费交易盘口信息ERROR={}", e);
            }
        }
    }

    /**
     * 订单取消成功
     *
     * @param records
     */
    @KafkaListener(topics = "exchange-order-cancel-success",containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCanceled(List<ConsumerRecord<String,String>> records) {
        for (int i = 0; i < records.size(); i++) {
            ConsumerRecord<String, String> record = records.get(i);
            try {
                logger.info("exchange-order-cancel-success topic={},accessKey={},value={}", record.topic(), record.key(), record.value());
                String symbol = record.key();
                ExchangeOrder order = JSON.parseObject(record.value(), ExchangeOrder.class);
                //调用服务处理
                MessageResult messageResult = exchangeOrderService.orderCanceled(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
                logger.info("取消订单成功messageResult={}",messageResult);
                //推送实时成交
                messagingTemplate.convertAndSend("/topic/market/order-canceled/" + symbol + "/" + order.getMemberId(), order);
                nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_CANCELED, order);
            } catch (Exception e) {
                logger.info("取消订单ERROR={}", e);
            }
        }
    }

    public class HandleTradeThread  implements Runnable{
        private ConsumerRecord<String, String> record;
        private HandleTradeThread(ConsumerRecord<String, String> record){
           this.record = record;
        }
        @Override
        public void run() {
            try {
                List<ExchangeTrade> trades = JSON.parseArray(record.value(), ExchangeTrade.class);
                String symbol = record.key();
                BigDecimal seRate = coinExchangeRate.getCoinLegalRate("USDT", "SE");
                HashMap<String, BigDecimal> usdRateMap = new HashMap<>();
                usdRateMap.put("SE", seRate);
                trades.stream().map(item -> Arrays.asList(item.getSymbol().split("/"))).flatMap(Collection::stream).distinct()
                        .forEach(coinSymbol->{
                            usdRateMap.put(coinSymbol, coinExchangeRate.getCoinLegalRate("USDT", coinSymbol));
                        });
                DataDictionary seFeeScaleDict = dataDictionaryService.findByBond(SysConstant.SE_MIN_SCALE);
                int seFeeScale = 0;
                try {
                    seFeeScale = Integer.parseInt(seFeeScaleDict.getValue());
                }catch (Exception ignored) {
                }
                for (ExchangeTrade trade : trades) {
                    //成交明细处理
                    exchangeOrderService.processExchangeTrade(trade, secondReferrerAward, usdRateMap, seFeeScale);
                    //推送订单成交订阅
                    ExchangeOrder buyOrder = exchangeOrderService.findOne(trade.getBuyOrderId());
                    ExchangeOrder sellOrder = exchangeOrderService.findOne(trade.getSellOrderId());
                    messagingTemplate.convertAndSend("/topic/market/order-trade/" + symbol + "/" + buyOrder.getMemberId(), buyOrder);
                    messagingTemplate.convertAndSend("/topic/market/order-trade/" + symbol + "/" + sellOrder.getMemberId(), sellOrder);
                    nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_TRADE, buyOrder);
                    nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_TRADE, sellOrder);
                }
            } catch (Exception e) {
                logger.info("撮合模块ERROR={}",e);
            }
        }
    }

    public String getUuid() {
        return uuid;
    }
}
