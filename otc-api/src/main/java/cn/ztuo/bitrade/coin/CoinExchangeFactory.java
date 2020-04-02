package cn.ztuo.bitrade.coin;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;

@Slf4j
public class CoinExchangeFactory {
    @Setter
    private HashMap<String,BigDecimal> cnyCoins;
    @Setter
    private HashMap<String,BigDecimal> jpyCoins;
    @Setter
    private HashMap<String,BigDecimal> hkdCoins;

    public HashMap<String,BigDecimal> getCnyCoins(){
        return cnyCoins;
    }

    public HashMap<String, BigDecimal> getJpyCoins() {
        return jpyCoins;
    }

    public HashMap<String, BigDecimal> getHkdCoins() {
        return hkdCoins;
    }

    public HashMap<String, BigDecimal> getCoins(String unit) {
        if(unit.equalsIgnoreCase("CNY")){
            return getCnyCoins();
        }
        else if(unit.equalsIgnoreCase("JPY")){
            return getJpyCoins();
        }
        else if(unit.equalsIgnoreCase("HKD")){
            return getHkdCoins();
        }
        else return null;
    }

    public CoinExchangeFactory(){
        cnyCoins = new HashMap<>();
        jpyCoins = new HashMap<>();
        hkdCoins = new HashMap<>();
    }

    /**
     * 获取币种价格
     * @param symbol
     * @return
     */
    public BigDecimal getCny(String symbol){
        return cnyCoins.get(symbol);
    }

    public void setCny(String symbol, BigDecimal rate){
        cnyCoins.put(symbol,rate);
    }

    public BigDecimal getJpy(String symbol){
        return jpyCoins.get(symbol);
    }

    public void setJpy(String symbol, BigDecimal rate){
        jpyCoins.put(symbol,rate);
    }

    public BigDecimal getHkd(String symbol){
        return hkdCoins.get(symbol);
    }

    public void setHkd(String symbol, BigDecimal rate){
        hkdCoins.put(symbol,rate);
    }

    public BigDecimal getLegalCurrencyRate(String currencyUnit,String symbol){
        if(currencyUnit.equalsIgnoreCase("CNY")){
            return getCny(symbol);
        }
        else if(currencyUnit.equalsIgnoreCase("JPY")){
            return getJpy(symbol);
        }
        else if(currencyUnit.equalsIgnoreCase("HKD")){
            return getHkd(symbol);
        }
        else return BigDecimal.ZERO;
    }
}
