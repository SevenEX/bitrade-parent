package cn.ztuo.bitrade.handler;

import cn.ztuo.bitrade.entity.CoinThumb;
import cn.ztuo.bitrade.entity.KLine;
import cn.ztuo.bitrade.entity.ExchangeTrade;
import cn.ztuo.bitrade.job.ExchangePushJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebsocketMarketHandler implements MarketHandler{
    @Autowired
    private ExchangePushJob pushJob;

    /**
     * 推送币种简化信息
     * @param symbol
     * @param exchangeTrade
     * @param thumb
     */
    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        //推送缩略行情
        pushJob.addThumb(symbol,thumb);
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        //推送K线数据
        pushJob.addKLine(symbol, kLine);
    }
}
