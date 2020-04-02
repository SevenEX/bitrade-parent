package cn.ztuo.bitrade.job;


import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.entity.ExchangeOrder;
import cn.ztuo.bitrade.entity.ExchangeOrderType;
import cn.ztuo.bitrade.service.ExchangeCoinService;
import cn.ztuo.bitrade.service.ExchangeOrderService;
import cn.ztuo.bitrade.util.CheckTraderOrderUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OrderUpdateJob {
    @Autowired
    private ExchangeOrderService orderService;
    @Autowired
    private ExchangeCoinService coinService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;
    private Logger logger = LoggerFactory.getLogger(OrderUpdateJob.class);

//    @Scheduled(fixedRate = 60*1000)
//    public void autoCancelOrder(){
    @XxlJob("autoCancelOrder")
    public ReturnT<String> autoCancelOrder(String param) throws Exception {
        logger.info("start autoCancelOrder...");
        List<ExchangeCoin> coinList = coinService.findAllEnabled();
        coinList.forEach(coin->{
            if(coin.getMaxTradingTime() > 0){
                List<ExchangeOrder> orders =  orderService.findOvertimeOrder(coin.getSymbol(),coin.getMaxTradingTime());
                orders.forEach(order -> {
                    // 发送消息至Exchange系统
                    if(order.getType()!=ExchangeOrderType.CHECK_FULL_STOP && CheckTraderOrderUtil.isExchangeOrderExist(order,restTemplate)) {
                        kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));
                    }else {
                        //强制取消
                        try {
                            orderService.forceCancelOrder(order);
                        } catch (Exception e) {
                            logger.info("强制取消异常={}",e);
                        }
                    }
                    logger.info("orderId:"+order.getOrderId()+",time:"+order.getTime());
                });
            }
        });
        logger.info("end autoCancelOrder...");
        return ReturnT.SUCCESS;
    }


}
