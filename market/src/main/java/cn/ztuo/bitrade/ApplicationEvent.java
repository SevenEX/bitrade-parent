package cn.ztuo.bitrade;

import cn.ztuo.bitrade.component.CoinExchangeRate;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.processor.CoinProcessor;
import cn.ztuo.bitrade.processor.CoinProcessorFactory;
import cn.ztuo.bitrade.service.ExchangeCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ApplicationEvent implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    ExchangeCoinService exchangeCoinService;
    @Autowired
    private CoinExchangeRate coinExchangeRate;
    @Value("${exchange.anchored-coins:USDT-USD}")
    private String legalAnchoredCoins;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("====初始化CoinExchangeRate====");
        String[] pairs  = legalAnchoredCoins.split(",");
        for(String pair:pairs){
            String[] symbol = pair.split("-");
            coinExchangeRate.getLegalAnchoredCoins().put(symbol[0].toUpperCase(),symbol[1].toUpperCase());
        }
        if(coinExchangeRate.getLegalAnchoredCoins().isEmpty()) {
            coinExchangeRate.getLegalAnchoredCoins().put("USDT", "USD");
        }
        try {
            coinExchangeRate.syncLegalRate();
        }
        catch (Exception e){
            log.info("同步coin汇率异常={}",e);
        }
        log.info("legalAnchoredCoins:{}",coinExchangeRate.getLegalAnchoredCoins());
        log.info("====初始化CoinProcessor====");
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        coins.forEach(coin->{
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            processor.initializeThumb();
            processor.initializeUsdRate();
            processor.setIsHalt(false);
        });
    }
}
