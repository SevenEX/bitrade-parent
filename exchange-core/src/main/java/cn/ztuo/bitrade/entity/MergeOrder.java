package cn.ztuo.bitrade.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
@Data
public class MergeOrder {

    public MergeOrder(){
    }
    private LinkedList<ExchangeOrder> orders = new LinkedList<>();

    //最后位置添加一个
    public void add(ExchangeOrder order){
        orders.addLast(order);
    }


    public ExchangeOrder get(){
        return orders.getFirst();
    }

    public int size(){
        return orders.size();
    }

    public BigDecimal getPrice(){
        return orders.getFirst().getPrice();
    }

    public Iterator<ExchangeOrder> iterator(){
        return orders.iterator();
    }
}
