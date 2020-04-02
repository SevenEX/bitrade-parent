package cn.ztuo.bitrade.controller;


import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.annotation.RedisCache;
import cn.ztuo.bitrade.component.CoinExchangeRate;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.Locale;
import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.processor.CoinProcessor;
import cn.ztuo.bitrade.processor.CoinProcessorFactory;
import cn.ztuo.bitrade.processor.CoinRankProcessor;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.RedisUtil;
import cn.ztuo.bitrade.waiting.WaitingOrder;
import cn.ztuo.bitrade.waiting.WaitingOrderFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@Api(tags = "行情")
public class MarketController extends BaseController {
    @Autowired
    private MarketService marketService;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private ExchangeTradeService exchangeTradeService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LeverCoinService leverCoinService;

    @Autowired
    private WaitingOrderFactory factory;
    @Autowired
    private DataDictionaryService dataDictionaryService;
    @Autowired
    private CoinExchangeRate coinExchangeRate;

    @Autowired
    private CoinRankProcessor coinRankProcessor;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CoinAreaService coinAreaService;
    @Autowired
    private LocalizationExtendService localizationExtendService;

    /**
     * 查询默认交易对儿
     *
     * @return
     */
    @RequestMapping(value = "default/symbol", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "获取 查询默认交易对")
    @MultiDataSource(name = "second")
    public MessageResult findDefaultSymbol() {
        Object redisResult = redisUtil.get(SysConstant.DEFAULT_SYMBOL);
        JSONObject jsonObject = new JSONObject();
        if (redisResult == null) {
            ExchangeCoin exchangeCoin = exchangeCoinService.findByDefault("1");
            if (exchangeCoin != null) {
                String result = exchangeCoin.getCoinSymbol() + "_" + exchangeCoin.getBaseSymbol();
                jsonObject.put("app", exchangeCoin.getSymbol().toUpperCase());
                jsonObject.put("web", result.toUpperCase());
                redisUtil.set(SysConstant.DEFAULT_SYMBOL, jsonObject);
                return success(jsonObject);
            } else {
                jsonObject.put("app", "BTC/USDT");
                jsonObject.put("web", "BTC_USDT");
                return success(jsonObject);
            }
        } else {
            return success(redisResult);
        }
    }


    /**
     * 查询待触发队列中信息
     *
     * @param symbol
     * @param orderId
     * @param direction
     * @return
     */
    @RequestMapping(value = "find/waiting", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "查询待触发队列中信息")
    @MultiDataSource(name = "second")
    public ExchangeOrder findWaitingOrder(String symbol, String orderId, ExchangeOrderDirection direction) {
        WaitingOrder waitingOrder = factory.getWaitingOrder(symbol);
        ExchangeOrder order = waitingOrder.findWaitingOrder(orderId, direction);
        return order;
    }

    /**
     * 获取支持的交易币种
     *
     * @return
     */
    @RequestMapping(value = "symbol", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "获取支持的交易币种")
    @MultiDataSource(name = "second")
    public List<ExchangeCoin> findAllSymbol() {
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        return coins;
    }

    /**
     * 获取支持的交易币种
     *
     * @return
     */
    @RequestMapping(value = "symbolByCoinSymbol", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "获取该币种支持的所有交易对")
    @MultiDataSource(name = "second")
    public List<String> symbolByCoinSymbol(String coinSymbol) {
        List<String> coins = exchangeCoinService.getExchangeSymbol(coinSymbol);
        return coins;
    }

    @RequestMapping(value = "overview", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "获取推荐的交易对")
    @MultiDataSource(name = "second")
    public Map<String, List<CoinThumb>> overview() {
        log.info("/market/overview");
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        long firstTimeOfToday = calendar.getTimeInMillis();
        Map<String, List<CoinThumb>> result = new HashMap<>();
        List<ExchangeCoin> recommendCoin = exchangeCoinService.findAllByFlag(1);
        List<CoinThumb> recommendThumbs = new ArrayList<>();
        for (ExchangeCoin coin : recommendCoin) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());

            if (processor != null) {
                CoinThumb thumb = processor.getThumb();
                List<KLine> lines = marketService.findAllKLine(thumb.getSymbol(), firstTimeOfToday, nowTime, "1hour");
                List<BigDecimal> trend = new ArrayList();
                for (KLine line : lines) {
                    trend.add(line.getClosePrice());
                }
                thumb.setTrend(trend);
                if(locale.equals(Locale.ZH_CN)){
                    String cnName = localizationExtendService.getLocaleInfo("Coin", locale, coin.getCoinSymbol(), "name");
                    thumb.setCnName(StringUtils.firstNonBlank(cnName, ""));
                }
                recommendThumbs.add(thumb);
            }
        }
        result.put("recommend", recommendThumbs);
        List<CoinThumb> allThumbs = findSymbolThumb(false,null);
        Collections.sort(allThumbs, (o1, o2) -> o2.getChg().compareTo(o1.getChg()));
        int limit = allThumbs.size() > 5 ? 5 : allThumbs.size();
        result.put("changeRank", allThumbs.subList(0, limit));
        return result;
    }


    /**
     * 获取某交易对详情
     *
     * @param symbol
     * @return
     */
    @RequestMapping(value = "symbol-info", method = RequestMethod.POST)
    @ApiOperation(value = "获取某交易对详情")
    @MultiDataSource(name = "second")
    public ExchangeCoin findSymbol(String symbol) {
        ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
        return coin;
    }

    /**
     * 获取币种缩略行情
     *
     * @return
     */
    @RequestMapping(value = "symbol-thumb", method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取币种缩略行情")
    @MultiDataSource(name = "second")
    public List<CoinThumb> findSymbolThumb(Boolean isLever,Integer areaId) {
        List<CoinThumb> thumbs = new ArrayList<>();
        if (isLever != null && isLever) {
            List<LeverCoin> leverCoinList = leverCoinService.findByEnable(BooleanEnum.IS_TRUE);
            for (LeverCoin leverCoin : leverCoinList) {
                CoinProcessor processor = coinProcessorFactory.getProcessor(leverCoin.getSymbol());
                if (processor != null) {
                    CoinThumb thumb = processor.getThumb();
                    thumb.setProportion(leverCoin.getProportion());
                    thumbs.add(thumb);
                }
            }
        } else {
            List<ExchangeCoin> coins;
            if(areaId==null || areaId == 0){
                coins = exchangeCoinService.findByCoin(null);
            }else{
                coins = exchangeCoinService.findByCoin(null,areaId);
            }
            //List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
            for (ExchangeCoin coin : coins) {
                CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
                if (processor != null) {
                    CoinThumb thumb = processor.getThumb();
                    thumbs.add(thumb);
                }
            }
        }
        return thumbs;
    }

    @RequestMapping(value = "symbol-thumb-trend", method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取币种行情")
    @MultiDataSource(name = "second")
    public JSONArray findSymbolThumbWithTrend(String coinName,Integer areaId) {
        List<ExchangeCoin> coins = new ArrayList<>();
        if(areaId==null || areaId == 0){
            coins = exchangeCoinService.findByCoin(coinName);
        }else{
            coins = exchangeCoinService.findByCoin(coinName,areaId);
        }

        //List<CoinThumb> thumbs = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        JSONArray array = new JSONArray();
        long firstTimeOfToday = calendar.getTimeInMillis();
        for (ExchangeCoin coin : coins) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            JSONObject json = (JSONObject) JSON.toJSON(thumb);
            json.put("zone", coin.getZone());
            List<KLine> lines = marketService.findAllKLine(thumb.getSymbol(), firstTimeOfToday, nowTime, "1hour");
            JSONArray trend = new JSONArray();
            for (KLine line : lines) {
                trend.add(line.getClosePrice());
            }
            json.put("trend", trend);
            json.put("areaId", coin.getAreaId());
            array.add(json);
        }
        return array;
    }

    @RequestMapping(value = "symbol-thumb-price", method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取Symbol价格")
    @MultiDataSource(name = "second")
    public JSONArray findSymbolThumbWithPrice(String symbol) {
        JSONArray array = new JSONArray();
        List<ExchangeCoin> coins;
        if(StringUtils.isEmpty(symbol)){
            coins = exchangeCoinService.findAllEnabled();
        }else {
            coins = exchangeCoinService.findByCoin(symbol);
        }
        for (ExchangeCoin coin : coins) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if (processor != null) {
                CoinThumb thumb = processor.getThumb();
                JSONObject json = new JSONObject();
                json.put("symbol",coin.getSymbol());
                json.put("price",thumb.getCloseStr());
                array.add(json);
            }
        }
        return array;
    }

    /**
     * 获取币种历史K线
     *
     * @param symbol
     * @param from
     * @param to
     * @param resolution
     * @return
     */
    @RequestMapping(value = "history", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "获取币种历史K线")
    public JSONArray findKHistory(String symbol, long from, long to, String resolution) {
        String period = "";
        if (resolution.endsWith("H") || resolution.endsWith("h")) {
            period = resolution.substring(0, resolution.length() - 1) + "hour";
        } else if (resolution.endsWith("D") || resolution.endsWith("d")) {
            period = resolution.substring(0, resolution.length() - 1) + "day";
        } else if (resolution.endsWith("W") || resolution.endsWith("w")) {
            period = resolution.substring(0, resolution.length() - 1) + "week";
        } else if (resolution.endsWith("M") || resolution.endsWith("m")) {
            period = resolution.substring(0, resolution.length() - 1) + "month";
        } else {
            Integer val = Integer.parseInt(resolution);
            if (val < 60) {
                period = resolution + "min";
            } else {
                period = (val / 60) + "hour";
            }
        }
        List<KLine> list = marketService.findAllKLine(symbol, from, to, period);

        JSONArray array = new JSONArray();
        for (KLine item : list) {
            JSONArray group = new JSONArray();
            group.add(0, item.getTime());
            group.add(1, item.getOpenPrice());
            group.add(2, item.getHighestPrice());
            group.add(3, item.getLowestPrice());
            group.add(4, item.getClosePrice());
            group.add(5, item.getVolume());
            group.add(6, item.getTurnover());
            group.add(7, item.getCount());
            array.add(group);
        }
        return array;
    }

    /**
     * 查询最近成交记录
     *
     * @param symbol 交易对符号
     * @param size   返回记录最大数量
     * @return
     */
    @RequestMapping(value = "latest-trade", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "查询最近成交记录")
    public List<ExchangeTrade> latestTrade(String symbol, int size) {
        // ExchangeCoin exchangeCoin = coinService.findBySymbol(symbol);
        List<ExchangeTrade> exchangeTrades = exchangeTradeService.findLatest(symbol, size);
        return exchangeTrades;
    }

    @RequestMapping(value = "exchange-plate", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "查询深度")
    public TradeAskBidDto<List<TradePlateItem>> findTradePlate(String symbol) {
        //远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-EXCHANGE-TRADE";
        String url = "http://" + serviceName + "/monitor/plate?symbol=" + symbol;
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<TradeAskBidDto<List<TradePlateItem>>>() {
        }).getBody();
    }

    @RequestMapping(value = "exchange-plate-mini", method = {RequestMethod.POST, RequestMethod.GET})
    public TradeAskBidDto<TradePlateDto> findTradePlateMini(String symbol) {
        //远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-EXCHANGE-TRADE";
        String url = "http://" + serviceName + "/monitor/plate-mini?symbol=" + symbol;
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<TradeAskBidDto<TradePlateDto>>() {
        }).getBody();
    }

    @RequestMapping(value = "exchange-plate-full", method = {RequestMethod.POST, RequestMethod.GET})
    public TradeAskBidDto<TradePlateDto> findTradePlateFull(String symbol) {
        //远程RPC服务URL,后缀为币种单位
        String serviceName = "SERVICE-EXCHANGE-TRADE";
        String url = "http://" + serviceName + "/monitor/plate-full?symbol=" + symbol;
        return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<TradeAskBidDto<TradePlateDto>>() {
        }).getBody();
    }

    @RequestMapping(value = "trade/snapshot", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "获取交易所今日交易估值")
    @MultiDataSource(name = "second")
    @RedisCache(RedissonKeyConstant.CACHE_TRADE_SNAPSHOT)
    public MessageResult getTradeSnapshot() {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("todayAmount", coinProcessorFactory.getProcessorMap().values().stream().filter(item -> item.getThumb() != null).map(processor -> {
            BigDecimal turnover = processor.getThumb().getTurnover();
            BigDecimal rate = coinExchangeRate.getCoinLegalRate("USD",processor.getBaseCoin());
            return turnover.multiply(rate==null?BigDecimal.ZERO:rate);
        }).reduce(BigDecimal.ZERO, BigDecimal::add));
        Coin coin = coinService.findByUnit("SE");
        BigDecimal todaySeAmount = BigDecimal.ZERO;
        BigDecimal seReleaseAmount = BigDecimal.ZERO;
        BigDecimal seBurnAmount = BigDecimal.ZERO;
        if(coin != null){
            todaySeAmount = coinProcessorFactory.getProcessorListByCoin("SE").stream().filter(item -> item.getThumb() != null).map(processor -> {
                BigDecimal turnover = processor.getThumb().getTurnover();
                BigDecimal rate = coinExchangeRate.getCoinLegalRate("USD",processor.getBaseCoin());
                return turnover.multiply(rate==null?BigDecimal.ZERO:rate);
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
            seReleaseAmount = coin.getReleaseAmount();
            seBurnAmount = coin.getBurnAmount();
        }
        map.put("seReleaseAmount", seReleaseAmount);
        map.put("todaySeAmount", todaySeAmount);
        map.put("seBurnAmount", seBurnAmount);
        return success(map);
    }

    @RequestMapping(value = "symbol-rank", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value = "获取交易所币种榜单")
    @ApiImplicitParams({
            @ApiImplicitParam(name="type", value = "类型", allowableValues = "0,1,2", required = true),
            @ApiImplicitParam(name="limit", value = "数量上限", required = true)
    })
    public MessageResult getSymbolRank(String type, int limit) {
        DataDictionary bond = dataDictionaryService.findByBond(SysConstant.HOME_PAGE_DISPLAY_QUANTITY);
        if(bond != null) {
            try {
                limit = Integer.parseInt(bond.getValue());
            } catch (Exception ignored) {}
        }
        switch (type){
            case "0":
                return success(coinRankProcessor.getChangeRankList(limit));
            case "1":
                return success(coinRankProcessor.getVolumeRankList(limit));
            case "2":
                return success(coinRankProcessor.getNewCoinRankList(limit));
            default:
                return success(coinRankProcessor.getChangeRankList(limit));
        }
    }

    @RequestMapping(value = "coinArea", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "获取 交易区")
    @MultiDataSource(name = "second")
    public MessageResult findCoinArea() {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        if(StringUtils.isEmpty(locale)){
            locale = Locale.ZH_CN;
        }
        Object redisResult = redisUtil.get(SysConstant.DEFAULT_AREA+locale);
        if (redisResult == null) {
            List<CoinArea> coinAreas = coinAreaService.findAll();
            List<CoinArea> coinArea = new ArrayList<>();
            for(CoinArea area:coinAreas){
                area.setName(localizationExtendService.getLocaleInfo("CoinArea",locale,area.getId().toString(),"name"));
                coinArea.add(area);
            }
            if (coinArea != null) {
                redisUtil.set(SysConstant.DEFAULT_AREA+locale, coinArea,10, TimeUnit.MINUTES);
                return success(coinArea);
            } else {
                return success("");
            }
        } else {
            return success(redisResult);
        }
    }
}
