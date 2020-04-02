package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.dto.BrokeInfoDto;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.service.ExchangeCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@RestController
@RequestMapping(value = "v1", method = {RequestMethod.GET, RequestMethod.POST})
public class V1Controller extends BaseController {
    @Autowired
    private ExchangeCoinService exchangeCoinService;

    @RequestMapping("brokerInfo")
    public BrokeInfoDto getBrokeInfo() {
        List<ExchangeCoin> exchangeCoins = exchangeCoinService.findAllEnabled();
        return BrokeInfoDto.builder()
                .timezone(TimeZone.getDefault().getID())
                .serverTime(String.valueOf(System.currentTimeMillis()))
                .brokerFilters(Collections.emptyList())
                .aggregates(Collections.emptyList())
                .options(Collections.emptyList())
                .contracts(Collections.emptyList())
                .symbols(exchangeCoins.stream().map(item->BrokeInfoDto.Symbol.builder()
                        .exchangeId(item.getSymbol())
                        .symbol(item.getSymbol())
                        .symbolName(item.getSymbol())
                        .baseAsset(item.getCoinSymbol())
                        .baseAssetPrecision(new BigDecimal("0.1").pow(item.getCoinScale()).toPlainString())
                        .quoteAsset(item.getBaseSymbol())
                        .quotePrecision(new BigDecimal("0.1").pow(item.getBaseCoinScale()).toPlainString())
                        .status("TRADING")
                        .icebergAllowed(false)
                        .filters(Arrays.asList(
                                BrokeInfoDto.PriceFilter.builder()
                                        .maxPrice("0")
                                        .minPrice(item.getMinSellPrice().toPlainString())
                                        .tickSize(item.getMinSellPrice().toPlainString())
                                        .build(),
                                BrokeInfoDto.LotSizeFilter.builder()
                                        .maxQty(item.getMaxVolume().toPlainString())
                                        .minQty(item.getMinVolume().toPlainString())
                                        .stepSize(new BigDecimal("0.1").pow(item.getBaseCoinScale()).toPlainString())
                                        .build(),
                                BrokeInfoDto.MinNotionalFilter.builder()
                                        .minNotional(item.getMinTurnover().toPlainString())
                                        .build()))
                        .build()).collect(Collectors.toList()))
                .rateLimits(Arrays.asList(
                        BrokeInfoDto.RateLimit.builder()
                                .rateLimitType("REQUEST_WEIGHT")
                                .interval("MINUTE")
                                .intervalUnit(1)
                                .limit(3000)
                                .build(),
                        BrokeInfoDto.RateLimit.builder()
                                .rateLimitType("ORDERS")
                                .interval("SECOND")
                                .intervalUnit(2)
                                .limit(40)
                                .build()))
                .build();
    }
}
