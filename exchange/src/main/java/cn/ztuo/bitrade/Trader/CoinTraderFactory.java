package cn.ztuo.bitrade.Trader;

import java.util.HashMap;

public class CoinTraderFactory {
    private HashMap<String,CoinTrader> traderMap;

    public CoinTraderFactory(){
        traderMap = new HashMap<>();
    }

    public void addTrader(String symbol,CoinTrader trader){
        traderMap.put(symbol,trader);
    }

    public CoinTrader getTrader(String symbol){
        return traderMap.get(symbol);
    }

    public HashMap<String, CoinTrader> getTraderMap() {
        return traderMap;
    }
}
