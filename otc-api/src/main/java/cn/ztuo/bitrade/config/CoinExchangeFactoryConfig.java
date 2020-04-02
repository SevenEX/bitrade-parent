package cn.ztuo.bitrade.config;

import cn.ztuo.bitrade.coin.CoinExchangeFactory;
import cn.ztuo.bitrade.entity.OtcCoin;
import cn.ztuo.bitrade.service.OtcCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@Slf4j
public class CoinExchangeFactoryConfig {

    @Bean
    public CoinExchangeFactory getCoinExchangeFactory(OtcCoinService coinService) {
        log.info("init CoinExchangeFactory start");
        List<OtcCoin> coins = coinService.findAll();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        coins.forEach(coin -> {
            factory.setCny(coin.getUnit(), new BigDecimal(0));
            log.info("factory add unit = {} , rate  = 0 !", coin.getUnit());
        });
        factory.setCny("USDT", new BigDecimal(0));
        log.info("factory add unit = USDT , rate  = 0!");
        log.info("init CoinExchangeFactory end");
        return factory;
    }
}
