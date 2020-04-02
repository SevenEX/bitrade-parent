package cn.ztuo.bitrade.job;

import cn.ztuo.bitrade.system.CoinExchangeFactory;
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
    private String serviceName = "bitrade-market";

//    @Scheduled(fixedRate = 60 * 1000)
//    public void syncRate() {
        @XxlJob("syncRate")
        public ReturnT<String> syncRate(String param) throws Exception {
        factory.getCoins().forEach((symbol, value) -> {
            BigDecimal usdRate = getCoinLegalRate("USD",symbol);
            BigDecimal cnyRate = getCoinLegalRate("CNY",symbol);
            BigDecimal sgdRate = getCoinLegalRate("SGD",symbol);
            factory.set(symbol, usdRate, cnyRate,sgdRate);
        });
            return ReturnT.SUCCESS;
    }

    /**
     * 获取某币种对法币的价格
     * @param unit
     * @return
     */
    private BigDecimal getCoinLegalRate(String legalCoin,String unit) {
        String url = "http://" + serviceName + "/market/exchange-rate/{legalCoin}/{coin}";
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class,legalCoin, unit);
        log.info("remote call:url={},baseCoin={},unit={},result={}", url,legalCoin,unit, result);
        if (result.getStatusCode().value() == 200 && result.getBody().getCode() == 0) {
            BigDecimal rate = new BigDecimal((String) result.getBody().getData());
            return rate;
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getUsdCnyRate() {
        String url = "http://" + serviceName + "/market/exchange-rate/usd-cny";
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
        log.info("remote call:url={},result={}", url, result);
        if (result.getStatusCode().value() == 200 && result.getBody().getCode() == 0) {
            BigDecimal rate = new BigDecimal(result.getBody().getData().toString());
            return rate;
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getUsdSgdRate() {
        String url = "http://" + serviceName + "/market/exchange-rate/usd-sgd";
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class);
        log.info("remote call:url={},result={}", url, result);
        if (result.getStatusCode().value() == 200 && result.getBody().getCode() == 0) {
            BigDecimal rate = new BigDecimal(result.getBody().getData().toString());
            return rate;
        } else {
            return BigDecimal.ZERO;
        }
    }
}
