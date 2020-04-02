package cn.ztuo.bitrade.config;

import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.service.ExchangeCoinService;
import cn.ztuo.bitrade.service.ExchangeOrderService;
import cn.ztuo.bitrade.waiting.WaitingOrder;
import cn.ztuo.bitrade.waiting.WaitingOrderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

/**
 * @description: WaitingOrderConfig
 * @author: MrGao
 * @create: 2019/04/27 15:30
 */
@Slf4j
@Configuration
public class WaitingOrderConfig {

    /**
     * 配置交易处理类
     * @param exchangeCoinService
     * @param kafkaTemplate
     * @return
     */
    @Bean
    public WaitingOrderFactory getWaitingOrderFactory(ExchangeCoinService exchangeCoinService, KafkaTemplate<String,String> kafkaTemplate, ExchangeOrderService exchangeOrderService){
        WaitingOrderFactory factory = new WaitingOrderFactory();
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        for(ExchangeCoin coin:coins) {
            log.info("init waiting order factory,symbol={}",coin.getSymbol());
            WaitingOrder waitingOrder = new WaitingOrder(coin.getSymbol());
            waitingOrder.initialize();
            factory.addWaitingOrder(coin.getSymbol(),waitingOrder);
            log.info("初始化结束={}",waitingOrder);
        }
        return factory;
    }
}
