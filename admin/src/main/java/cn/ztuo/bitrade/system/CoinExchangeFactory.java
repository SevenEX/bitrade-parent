package cn.ztuo.bitrade.system;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;

@Slf4j
public class CoinExchangeFactory {

    @Data
    public class ExchangeRate{
        public BigDecimal cnyRate;
        public BigDecimal usdRate;
        public BigDecimal sgdRate;
        public BigDecimal btcRate;
    }


    @Setter
    private HashMap<String,ExchangeRate> coins;

    public HashMap<String,ExchangeRate> getCoins(){
        return coins;
    }

    public CoinExchangeFactory(){
        coins = new HashMap<>();
    }


    /**
     * 获取币种价格
     * @param symbol
     * @return
     */
    public ExchangeRate get(String symbol){
        return coins.get(symbol);
    }

    public void set(String symbol,BigDecimal usdRate,BigDecimal cnyRate,BigDecimal sgRate){
        ExchangeRate rate = new ExchangeRate();
        rate.setCnyRate(cnyRate);
        rate.setUsdRate(usdRate);
        rate.setSgdRate(sgRate);
        coins.put(symbol,rate);
    }
}
