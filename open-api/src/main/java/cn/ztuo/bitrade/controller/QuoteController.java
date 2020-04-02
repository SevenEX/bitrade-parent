package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.dto.TickerDto;
import cn.ztuo.bitrade.dto.TradeDto;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.exception.GeneralException;
import cn.ztuo.bitrade.service.ApiMarketService;
import cn.ztuo.bitrade.service.ExchangeTradeService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Api(tags = "行情")
@RestController
@RequestMapping(value = "quote", method = {RequestMethod.GET, RequestMethod.POST})
@Slf4j
public class QuoteController extends BaseController {
    @Autowired
    private ApiMarketService apiMarketService;

    @Autowired
    private ExchangeTradeService exchangeTradeService;
    @Autowired
    private RestTemplate restTemplate;

    @ApiOperation("获取24小时行情")
    @RequestMapping(value = "/v1/ticker/24hr")
    public List<TickerDto> getTicker24Hr(String symbol) throws Exception {
        List<String> symbols = Collections.singletonList(symbol);
        if (StringUtils.isEmpty(symbol)) {
            String serviceName = "BITRADE-MARKET";
            String url = "http://" + serviceName + "/market/symbol-thumb";
            List<CoinThumb> result = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<CoinThumb>>() {
            }).getBody();
            if (result == null) {
                symbols = Collections.emptyList();
            } else {
                symbols = result.stream().map(CoinThumb::getSymbol).collect(Collectors.toList());
            }
        }
        try {
            long time = DateUtil.dateAddDay(new Date(), -1).getTime();
            return symbols.stream().map(item -> {
                List<KLine> list = apiMarketService.findLatestKLine(item, time, System.currentTimeMillis(), "1min");
                if (list.size() > 0) {
                    list.get(0).setSymbol(item);
                    return list.get(0);
                }
                return null;
            }).filter(Objects::nonNull).map(item -> TickerDto.builder()
                    .symbol(item.getSymbol())
                    .highPrice(item.getHighestPrice().toPlainString())
                    .lowPrice(item.getLowestPrice().toPlainString())
                    .lastPrice(item.getClosePrice().toPlainString())
                    .openPrice(item.getOpenPrice().toPlainString())
                    .quoteVolume(item.getTurnover().toPlainString())
                    .volume(item.getVolume().toPlainString())
                    .time(item.getTime())
                    .build()).collect(Collectors.toList());
        } catch (Exception e) {
            log.info(">>>>>>>获取K线图异常>>>>>", e);
            throw new GeneralException(msService.getMessage("GET_HISTORY_ERROR"));
        }
    }

    @ApiOperation("币种最近成交")
    @RequestMapping("/v1/trades")
    public List<TradeDto> getTrades(String symbol, Integer limit) throws Exception {
        if(limit == null){
            limit = 500;
        }
        else if(limit > 1000) {
            limit = 1000;
        }
        return exchangeTradeService.findLatest(symbol, limit).stream().filter(Objects::nonNull).map(item->TradeDto.builder()
                .time(item.getTime())
                .qty(item.getAmountStr())
                .price(item.getPriceStr())
                .isBuyerMaker(item.getIsBuyerMaker())
                .build()).collect(Collectors.toList());
    }

    /**
     *  获取盘口信息
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "订单簿")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "币种", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "条数", required = false, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "/v1/depth")
    public Map getTradePlateInfo(@RequestParam("symbol")String symbol, @RequestParam("limit")Integer limit)throws  GeneralException{
        if(limit == null || limit > 100){
            limit = 100;
        }
        Map<String,Object> result = new HashMap<>();
        try {
            String serviceName = "SERVICE-EXCHANGE-TRADE";
            String url = "http://" + serviceName + "/monitor/plate?symbol="+symbol;
            TradeAskBidDto<List<TradePlateItem>> askBidDto = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<TradeAskBidDto<List<TradePlateItem>>>() {
            }).getBody();
            List<TradePlateItem> askList = askBidDto.getAsk();
            List<TradePlateItem> bidList = askBidDto.getBid();
            if(bidList.size()<limit) {
                limit = bidList.size();
                String[][] moduleList = new String[limit][2];
                for (int i = 0; i < limit; i++) {
                    TradePlateItem bid = bidList.get(i);
                    moduleList[i][0] = bid.getPriceStr();
                    moduleList[i][1] = bid.getAmountStr();
                }
                result.put("bids", moduleList);
            }
            if(askList.size()<limit) {
                limit = askList.size();
                String[][] moduleList = new String[limit][2];
                for (int i = 0; i < limit; i++) {
                    TradePlateItem ask = askList.get(i);
                    moduleList[i][0] = ask.getPriceStr();
                    moduleList[i][1] = ask.getAmountStr();
                }
                result.put("asks", moduleList);
            }
        }catch (Exception e){
            log.info(">>>>>>>获取订单簿信息异常>>>>>>>",e);
            throw new GeneralException("GET_TRADE_PLATE_ERROR",e);
        }
        return  result;
    }

    /**
     * 获取历史K线图 根据时间
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "k线/烛线图数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "period", value = "", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "", required = true, dataType = "int", paramType = "query"),
    })
    @RequestMapping(value = "/v1/klines")
    public MessageResult getHistoryKline(@RequestParam("symbol")String symbol, @RequestParam("period")String period , @RequestParam("size") int size)throws GeneralException{
        List<KLine> list;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH,-1);
            list = apiMarketService.findAllKLine(symbol,calendar.getTimeInMillis(),System.currentTimeMillis(),period);
            if(list.size()>size){
                list=list.subList(0,size);
            }
        } catch (Exception e) {
            log.info(">>>>>>>获取K线图异常>>>>>",e);
            throw new GeneralException(msService.getMessage("GET_HISTORY_ERROR"));
        }
        return success(list);
    }

    /**
     * Symbol价格
     * @return
     * @throws GeneralException
     */
    @ApiOperation(value = "Symbol价格")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "币种", required = false, dataType = "String", paramType = "query"),
    })
    @RequestMapping(value = "/v1/ticker/price")
    public JSONArray getCoinSymbol(@RequestParam("symbol")String symbol)throws  GeneralException{
        JSONArray array;
        try {
            //远程RPC服务URL,后缀为币种单位
            String serviceName = "BITRADE-MARKET";
            String url = "http://" + serviceName + "/market/symbol-thumb-price?symbol="+symbol;
            ResponseEntity<JSONArray> result = restTemplate.getForEntity(url,JSONArray.class);
            log.info("remote call:service={},result={}", serviceName, result);
            array = result.getBody();
        } catch (Exception e) {
            log.info(">>>>>>获取Symbol价格异常>>>>>>"+e);
            throw new GeneralException(msService.getMessage("GET_COIN_SYMBOL_ERROR"));
        }
        return array;
    }
}