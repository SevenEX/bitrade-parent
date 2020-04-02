package cn.ztuo.bitrade.job;

import cn.ztuo.bitrade.coin.CoinExchangeFactory;
import cn.ztuo.bitrade.util.MessageResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@Slf4j
public class CheckExchangeRate {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CoinExchangeFactory factory;

//    @Scheduled(fixedRate = 5 * 60 * 1000)
//    public void syncRate() {
    @XxlJob("syncRate")
    public ReturnT<String> syncRate(String param) throws Exception {
        log.info("CheckExchangeRate syncRate start");
        factory.getCnyCoins().forEach(
                (symbol, value) -> {
                    String serviceName = "bitrade-market";
                    String url = "http://" + serviceName + "/market/exchange-rate/cny/{coin}";
                    ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, symbol);
                    log.info("remote call:url={},result={},unit={}", url, result, symbol);
                    if (result.getStatusCode().value() == 200 && result.getBody().getCode() == 0) {
                        BigDecimal rate = new BigDecimal((String) result.getBody().getData());
                        factory.setCny(symbol, rate);
                    } else {
                        factory.setCny(symbol, BigDecimal.ZERO);
                    }
                    String url2 = "http://" + serviceName + "/market/exchange-rate/jpy/{coin}";
                    ResponseEntity<MessageResult> result2 = restTemplate.getForEntity(url2, MessageResult.class, symbol);
                    log.info("remote call:url={},result={},unit={}", url2, result2, symbol);
                    if (result2.getStatusCode().value() == 200 && result2.getBody().getCode() == 0) {
                        BigDecimal rate = new BigDecimal((String) result2.getBody().getData());
                        factory.setJpy(symbol, rate);
                    } else {
                        factory.setJpy(symbol, BigDecimal.ZERO);
                    }

                    String url3 = "http://" + serviceName + "/market/exchange-rate/hkd/{coin}";
                    ResponseEntity<MessageResult> result3 = restTemplate.getForEntity(url3, MessageResult.class, symbol);
                    log.info("remote call:url={},result={},unit={}", url3, result3, symbol);
                    if (result3.getStatusCode().value() == 200 && result3.getBody().getCode() == 0) {
                        BigDecimal rate = new BigDecimal((String) result3.getBody().getData());
                        factory.setHkd(symbol, rate);
                    } else {
                        factory.setHkd(symbol, BigDecimal.ZERO);
                    }
                });
        log.info("CheckExchangeRate syncRate end");
        return ReturnT.SUCCESS;
    }
}
