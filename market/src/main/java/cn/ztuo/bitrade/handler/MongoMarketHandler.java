package cn.ztuo.bitrade.handler;

import cn.ztuo.bitrade.consumer.ExchangeTradeConsumer;
import cn.ztuo.bitrade.entity.CoinThumb;
import cn.ztuo.bitrade.entity.ExchangeTrade;
import cn.ztuo.bitrade.entity.KLine;
import cn.ztuo.bitrade.util.SpringContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoMarketHandler implements MarketHandler {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        if(StringUtils.isEmpty(exchangeTrade.getSenderUuid()) || StringUtils.equals(SpringContextUtil.getBean(ExchangeTradeConsumer.class).getUuid(), exchangeTrade.getSenderUuid())) {
            mongoTemplate.insert(exchangeTrade, "exchange_trade_" + symbol);
        }
    }
    @Override
    public void handleKLine(String symbol,KLine kLine) {
        mongoTemplate.insert(kLine,"exchange_kline_"+symbol+"_"+kLine.getPeriod());
    }
}
