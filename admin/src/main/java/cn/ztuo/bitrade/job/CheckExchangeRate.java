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
import java.math.RoundingMode;

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
        BigDecimal cnyRate = getUsdCnyRate();
        BigDecimal sgdRate = getUsdSgdRate();
        BigDecimal btcRate = getUsdRate("BTC");
        factory.getCoins().forEach((symbol, value) -> {
            BigDecimal usdRate = getUsdRate(symbol);
            BigDecimal usdBtc=btcRate.compareTo(BigDecimal.ZERO)==0?BigDecimal.ZERO:usdRate.divide(btcRate,4, BigDecimal.ROUND_UP);
            factory.set(symbol, usdRate, cnyRate.multiply(usdRate).setScale(4, RoundingMode.UP)
                    ,sgdRate.multiply(usdRate).setScale(4, RoundingMode.UP));
        });
        return ReturnT.SUCCESS;
    }

    private BigDecimal getUsdRate(String unit) {
        String url = "http://" + serviceName + "/market/exchange-rate/usd/{coin}";
        ResponseEntity<MessageResult> result = restTemplate.getForEntity(url, MessageResult.class, unit);
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
        if (result.getStatusCode().value() == 200 && result.getBody().getCode() == 0) {
            BigDecimal rate = new BigDecimal(result.getBody().getData().toString());
            return rate;
        } else {
            return BigDecimal.ZERO;
        }
    }
}
