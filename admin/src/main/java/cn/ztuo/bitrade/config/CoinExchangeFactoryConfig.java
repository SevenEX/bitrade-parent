package cn.ztuo.bitrade.config;

import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CoinExchangeFactoryConfig {
    @Autowired
    private CoinService coinService;

    @Bean
    public CoinExchangeFactory createCoinExchangeFactory() {
        List<Coin> coinList = coinService.findAll();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        coinList.forEach(coin ->
                factory.set(coin.getUnit(), coin.getUsdRate(), coin.getCnyRate(),coin.getSgdRate())
        );
        return factory;
    }
}
