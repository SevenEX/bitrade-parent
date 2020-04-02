package cn.ztuo.bitrade.controller;

import com.alibaba.fastjson.JSONObject;
import cn.ztuo.bitrade.Trader.CoinTrader;
import cn.ztuo.bitrade.Trader.CoinTraderFactory;
import cn.ztuo.bitrade.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/monitor")
public class MonitorController {
    @Autowired
    private CoinTraderFactory factory;

    @RequestMapping(value = "trader-overview", produces="application/json")
    public JSONObject traderOverview(String symbol){
        CoinTrader trader = factory.getTrader(symbol);
        if(trader == null )return null;
        JSONObject result = new JSONObject();
        //卖盘信息
        JSONObject ask = new JSONObject();
        //买盘信息
        JSONObject bid = new JSONObject();
        ask.put("limit_price_order_count",trader.getLimitPriceOrderCount(ExchangeOrderDirection.SELL));
        ask.put("market_price_order_count",trader.getSellMarketQueue().size());
        ask.put("depth",trader.getTradePlate(ExchangeOrderDirection.SELL).getDepth());
        bid.put("limit_price_order_count",trader.getLimitPriceOrderCount(ExchangeOrderDirection.BUY));
        bid.put("market_price_order_count",trader.getBuyMarketQueue().size());
        bid.put("depth",trader.getTradePlate(ExchangeOrderDirection.BUY).getDepth());
        result.put("ask",ask);
        result.put("bid",bid);
        return result;
    }

    @RequestMapping(value = "trader-detail", produces="application/json")
    public TradeAskBidDto<TradeDetailDto> traderDetail(String symbol){
        CoinTrader trader = factory.getTrader(symbol);
        if(trader == null )return null;
        TradeAskBidDto<TradeDetailDto> askBidDto = new TradeAskBidDto<>();
        TradeDetailDto askDto = new TradeDetailDto();
        askDto.setLimitPriceQueue(trader.getSellLimitPriceQueue());
        askDto.setMarketPriceQueue(trader.getSellMarketQueue());
        askBidDto.setAsk(askDto);

        TradeDetailDto bidDto = new TradeDetailDto();
        bidDto.setLimitPriceQueue(trader.getBuyLimitPriceQueue());
        bidDto.setMarketPriceQueue(trader.getBuyMarketQueue());
        askBidDto.setBid(bidDto);
        return askBidDto;
    }


    @RequestMapping(value = "plate", produces="application/json")
    public TradeAskBidDto<List<TradePlateItem>> traderPlate(String symbol){
        CoinTrader trader = factory.getTrader(symbol);
        if(trader == null )return null;
        TradeAskBidDto<List<TradePlateItem>> askBidDto = new TradeAskBidDto<>();
        askBidDto.setBid(trader.getTradePlate(ExchangeOrderDirection.BUY).getItems());
        askBidDto.setAsk(trader.getTradePlate(ExchangeOrderDirection.SELL).getItems());
        return askBidDto;
    }

    @RequestMapping(value = "plate-mini", produces="application/json")
    public TradeAskBidDto<TradePlateDto> traderPlateMini(String symbol){
        CoinTrader trader = factory.getTrader(symbol);
        if(trader == null )return null;
        TradeAskBidDto<TradePlateDto> askBidDto = new TradeAskBidDto<>();
        askBidDto.setBid(trader.getTradePlate(ExchangeOrderDirection.BUY).toTradePlateDto(24));
        askBidDto.setAsk(trader.getTradePlate(ExchangeOrderDirection.SELL).toTradePlateDto(24));
        return askBidDto;
    }

    @RequestMapping(value = "plate-full", produces="application/json")
    public TradeAskBidDto<TradePlateDto> traderPlateFull(String symbol){
        CoinTrader trader = factory.getTrader(symbol);
        if(trader == null )return null;
        TradeAskBidDto<TradePlateDto> askBidDto = new TradeAskBidDto<>();
        askBidDto.setBid(trader.getTradePlate(ExchangeOrderDirection.BUY).toTradePlateDto());
        askBidDto.setAsk(trader.getTradePlate(ExchangeOrderDirection.SELL).toTradePlateDto());
        return askBidDto;
    }

    @RequestMapping(value = "symbols", produces="application/json")
    public List<String> symbols(){
        HashMap<String,CoinTrader>  traders = factory.getTraderMap();
        List<String> symbols = new ArrayList<>();
        traders.forEach((key,trader) ->{
            symbols.add(key);
        });
        return symbols;
    }

    /**
     * 查找订单
     * @param symbol
     * @param orderId
     * @param direction
     * @param type
     * @return
     */
    @RequestMapping(value = "order")
    public ExchangeOrder findOrder(String symbol,String orderId, ExchangeOrderDirection direction, ExchangeOrderType type){
        CoinTrader trader = factory.getTrader(symbol);
        return trader.findOrder(orderId,type,direction);
    }
}
