package cn.ztuo.bitrade.entity;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

/**
 * 盘口信息
 */
@Data
@Slf4j
public class TradePlate {
    private LinkedList<TradePlateItem> items;
    //最大深度
    private int maxDepth = 1000;
    private int coinScale;
    private int baseCoinScale;
    //方向
    private ExchangeOrderDirection direction;
    private String symbol;
    public TradePlate(){

    }

    public TradePlate(String symbol,ExchangeOrderDirection direction) {
        this.direction = direction;
        this.symbol = symbol;
        items = new LinkedList<>();
    }

    public boolean add(ExchangeOrder exchangeOrder) {
        log.info("add TradePlate order={}",exchangeOrder);
        synchronized (items) {
            int index = 0;
            if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE) {
                return false;
            }
            if (exchangeOrder.getDirection() != direction) {
                return false;
            }
            if (items.size() > 0) {
                for (index = 0; index < items.size(); index++) {
                    TradePlateItem item = items.get(index);
                    if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY && item.getPrice().compareTo(exchangeOrder.getPrice()) > 0
                            || exchangeOrder.getDirection() == ExchangeOrderDirection.SELL && item.getPrice().compareTo(exchangeOrder.getPrice()) < 0) {
                        continue;
                    } else if (item.getPrice().compareTo(exchangeOrder.getPrice()) == 0) {
                        BigDecimal deltaAmount = exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount());
                        item.setAmount(item.getAmount().add(deltaAmount));
                        item.setAmountStr(item.getAmount().setScale(coinScale,RoundingMode.DOWN).toPlainString());
                        return true;
                    } else {
                        break;
                    }
                }
            }
            if(index < maxDepth) {
                TradePlateItem newItem = new TradePlateItem();
                newItem.setAmount(exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount()));
                newItem.setAmountStr(newItem.getAmount().setScale(coinScale,RoundingMode.DOWN).toPlainString());
                newItem.setPrice(exchangeOrder.getPrice());
                newItem.setPriceStr(exchangeOrder.getPrice().setScale(baseCoinScale, RoundingMode.DOWN).toPlainString());
                items.add(index, newItem);
            }
        }
        return true;
    }

    public void remove(ExchangeOrder order,BigDecimal amount) {
        synchronized (items) {
            for (int index = 0; index < items.size(); index++) {
                TradePlateItem item = items.get(index);
                if (item.getPrice().compareTo(order.getPrice()) == 0) {
                    item.setAmount(item.getAmount().subtract(amount));
                    item.setAmountStr(item.getAmount().setScale(coinScale,RoundingMode.DOWN).toPlainString());
                    if (item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        items.remove(index);
                    }
                    return;
                }
            }
        }
    }

    public void remove(ExchangeOrder order){
        remove(order,order.getAmount().subtract(order.getTradedAmount()));
    }

    public void setItems(LinkedList<TradePlateItem> items){
        this.items = items;
    }

    public BigDecimal getHighestPrice(){
        if(items.size() == 0)return BigDecimal.ZERO;
        if(direction == ExchangeOrderDirection.BUY){
            return items.getFirst().getPrice();
        }
        else{
            return items.getLast().getPrice();
        }
    }

    public int getDepth(){
        return items.size();
    }


    public BigDecimal getLowestPrice(){
        if(items.size() == 0)return BigDecimal.ZERO;
        if(direction == ExchangeOrderDirection.BUY){
            return items.getLast().getPrice();
        }
        else{
            return items.getFirst().getPrice();
        }
    }

    /**
     * 获取委托量最大的档位
     * @return
     */
    public BigDecimal getMaxAmount(){
        if(items.size() == 0)return BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        for(TradePlateItem item:items){
            if(item.getAmount().compareTo(amount)>0){
                amount = item.getAmount();
            }
        }
        return amount;
    }

    /**
     * 获取委托量最小的档位
     * @return
     */
    public BigDecimal getMinAmount(){
        if(items.size() == 0)return BigDecimal.ZERO;
        BigDecimal amount = items.getFirst().getAmount();
        for(TradePlateItem item:items){
            if(item.getAmount().compareTo(amount) < 0){
                amount = item.getAmount();
            }
        }
        return amount;
    }

    public TradePlateDto toTradePlateDto(){
        TradePlateDto dto = new TradePlateDto();
        dto.setDirection(direction.ordinal());
        dto.setDirection(direction.ordinal());
        dto.setMaxAmount(getMaxAmount());
        dto.setMinAmount(getMinAmount());
        dto.setHighestPrice(getHighestPrice());
        dto.setLowestPrice(getLowestPrice());
        dto.setSymbol(getSymbol());
        dto.setItems(items);
        return dto;
    }

    public TradePlateDto toTradePlateDto(int limit){
        TradePlateDto dto = new TradePlateDto();
        dto.setDirection(direction.ordinal());
        dto.setDirection(direction.ordinal());
        dto.setMaxAmount(getMaxAmount());
        dto.setMinAmount(getMinAmount());
        dto.setHighestPrice(getHighestPrice());
        dto.setLowestPrice(getLowestPrice());
        dto.setSymbol(getSymbol());
        dto.setItems(items.size() > limit ? items.subList(0,limit) : items);
        return dto;
    }
}
