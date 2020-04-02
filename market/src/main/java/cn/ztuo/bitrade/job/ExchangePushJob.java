package cn.ztuo.bitrade.job;

import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.handler.NettyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ExchangePushJob {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private NettyHandler nettyHandler;
    private Map<String,List<ExchangeTrade>> tradesQueue = new HashMap<>();
    private Map<String,List<TradePlate>> plateQueue = new HashMap<>();
    private Map<String,List<CoinThumb>> thumbQueue = new HashMap<>();
    private Map<String, KLine> klineQueue = new ConcurrentHashMap<>();

    public void addTrades(String symbol, List<ExchangeTrade> trades){
        List<ExchangeTrade> list = tradesQueue.get(symbol);
        if(list == null){
            list = new ArrayList<>();
            tradesQueue.put(symbol,list);
        }
        synchronized (list) {
            list.addAll(trades);
        }
    }

    public void addPlates(String symbol, TradePlate plate){
        List<TradePlate> list = plateQueue.get(symbol);
        if(list == null){
            list = new ArrayList<>();
            plateQueue.put(symbol,list);
        }
        synchronized (list) {
            list.add(plate);
        }
    }

    public void addThumb(String symbol, CoinThumb thumb){
        List<CoinThumb> list = thumbQueue.get(symbol);
        if(list == null){
            list = new ArrayList<>();
            thumbQueue.put(symbol,list);
        }
        synchronized (list) {
            list.add(thumb);
        }
    }

    public void addKLine(String symbol, KLine kLine) {
        String key;
        if (StringUtils.equals("1min", kLine.getPeriod())) {
            key = symbol;
        } else {
            key = symbol + "/" + kLine.getPeriod();
        }
        klineQueue.put(key, kLine);
    }

    @Scheduled(fixedRate = 500)
    public void pushTrade(){
        Iterator<Map.Entry<String,List<ExchangeTrade>>> entryIterator = tradesQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<ExchangeTrade>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<ExchangeTrade> trades = entry.getValue();
            if(trades.size() > 0){
                synchronized (trades) {
                    //一次最大推送100条成交
                    int maxLength = 100;
                    List<ExchangeTrade> pushTrades = trades.size() > maxLength ? trades.subList(0,maxLength): trades;
                    messagingTemplate.convertAndSend("/topic/market/trade/" + symbol, pushTrades);
                    trades.clear();
                }
            }
        }
    }


    @Scheduled(fixedRate = 500)
    public void pushPlate(){
        Iterator<Map.Entry<String,List<TradePlate>>> entryIterator = plateQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<TradePlate>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<TradePlate> plates = entry.getValue();
            if(plates.size() > 0){
                boolean hasPushAskPlate = false;
                boolean hasPushBidPlate = false;
                synchronized (plates) {
                    for(TradePlate plate:plates) {
                        log.info("====处理盘口推送信息===");
                        if(plate.getDirection() == ExchangeOrderDirection.BUY && !hasPushBidPlate) {
                            hasPushBidPlate = true;
                        }
                        else if(plate.getDirection() == ExchangeOrderDirection.SELL && !hasPushAskPlate){
                            hasPushAskPlate = true;
                        }else {
                            continue;
                        }
                        //websocket推送盘口信息
                        messagingTemplate.convertAndSend("/topic/market/trade-plate/" + symbol, plate.toTradePlateDto(24));
                        //websocket推送深度信息
                        messagingTemplate.convertAndSend("/topic/market/trade-depth/" + symbol, plate.toTradePlateDto());
                        //netty推送
                        nettyHandler.handlePlate(symbol, plate);
                    }
                    plates.clear();
                }
            }
        }
    }


    @Scheduled(fixedRate = 500)
    public void pushThumb(){
        Iterator<Map.Entry<String,List<CoinThumb>>> entryIterator = thumbQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<CoinThumb>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<CoinThumb> thumbs = entry.getValue();
            if(thumbs.size() > 0){
                synchronized (thumbs) {
                    CoinThumb thumb = thumbs.get(thumbs.size() - 1);
                    messagingTemplate.convertAndSend("/topic/market/thumb",thumb);
                    nettyHandler.pushThumb(symbol,thumb);
                    thumbs.clear();
                }
            }
        }
    }
    @Scheduled(fixedRate = 500)
    public void pushKline() {
        Iterator<Map.Entry<String, KLine>> entryIterator = klineQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String, KLine> entry =  entryIterator.next();
            messagingTemplate.convertAndSend("/topic/market/kline/" + entry.getKey(), entry.getValue());
            nettyHandler.pushKLine(entry.getKey(), entry.getValue());
            entryIterator.remove();
        }
    }
}
