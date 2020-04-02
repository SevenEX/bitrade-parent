package cn.ztuo.bitrade.waiting;

import java.util.HashMap;

/**
 * @description: WaitingOrderFactory
 * @author: MrGao
 * @create: 2019/04/27 17:07
 */
public class WaitingOrderFactory {

    private HashMap<String,WaitingOrder> waitingOrderHashMap ;
    public WaitingOrderFactory(){
        waitingOrderHashMap = new HashMap<>();
    }

    public void addWaitingOrder(String symbol,WaitingOrder waitingOrder){
        waitingOrderHashMap.put(symbol,waitingOrder);
    }

    public WaitingOrder getWaitingOrder(String symbol){
        return waitingOrderHashMap.get(symbol);
    }

    public HashMap<String, WaitingOrder> getWaitingOrderHashMap() {
        return waitingOrderHashMap;
    }

}
