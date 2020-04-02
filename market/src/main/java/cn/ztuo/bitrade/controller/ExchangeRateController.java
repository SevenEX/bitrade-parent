package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.component.CoinExchangeRate;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/exchange-rate")
@Api(tags = "汇率/价格")
public class ExchangeRateController {
    @Autowired
    private CoinExchangeRate coinExchangeRate;

    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * 获取 交易币对法币的价格
     * @param legalCoin 法币
     * @param coin 交易币
     * @return
     */
    @RequestMapping(value = "{legalCoin}/{coin}",method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "获取 交易币对法币的价格")
    @MultiDataSource(name = "second")
    public MessageResult getUsdExchangeRate(@PathVariable String legalCoin,@PathVariable String coin){
        MessageResult mr = new MessageResult(0,messageSource.getMessage("SUCCESS"));
        BigDecimal latestPrice = coinExchangeRate.getCoinLegalRate(legalCoin.toUpperCase(),coin.toUpperCase());
        mr.setData(latestPrice==null?"0":latestPrice.toString());
        return mr;
    }

    /**
     * 获取法币之间的汇率
     * @param fromUnit
     * @param toUnit
     * @return
     */
    @RequestMapping(value = "{fromUnit}-{toUnit}",method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "获取法币之间的汇率")
    @MultiDataSource(name = "second")
    public MessageResult getUsdCnyRate(@PathVariable String fromUnit,@PathVariable String toUnit){
        MessageResult mr = new MessageResult(0,messageSource.getMessage("SUCCESS"));
        mr.setData(coinExchangeRate.getLegalRate(fromUnit.toUpperCase(),toUnit.toUpperCase()));
        return mr;
    }
}
