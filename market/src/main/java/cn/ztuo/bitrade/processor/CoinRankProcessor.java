package cn.ztuo.bitrade.processor;

import cn.ztuo.bitrade.entity.CoinThumb;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.service.ExchangeCoinService;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CoinRankProcessor {
    private static CoinRankProcessor processor;

    private ConcurrentHashMap<String, CoinThumb> symbolMap = new ConcurrentHashMap<>();

    /**
     * 涨幅榜
     */
    private List<RankDto> changeRankList;
    /**
     * 成交榜
     */
    private List<RankDto> volumeRankList;
    /**
     * 新币榜
     */
    private List<RankDto> newCoinRankList;

    @Autowired
    private ExchangeCoinService exchangeCoinService;

    @Autowired
    private CoinProcessorFactory coinProcessorFactory;

    @PostConstruct
    public void init() {
        processor = this;
        changeRankList = new ArrayList<>();
        volumeRankList = new ArrayList<>();
        newCoinRankList = new ArrayList<>();
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void cacheRankList() {
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        HashMap<String, String> coinSymbolMap = new HashMap<>();
        HashMap<String, Boolean> defaultSymbolMap = new HashMap<>();
        symbolMap.clear();
        for (ExchangeCoin coin : coins) {
            CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
            if (processor != null) {
                CoinThumb thumb = processor.getThumb();
                if (thumb != null) {
                    symbolMap.put(thumb.getSymbol(), thumb);
                }
            }
            coinSymbolMap.put(coin.getSymbol(), coin.getCoinSymbol());
            defaultSymbolMap.put(coin.getSymbol(), !StringUtils.equals(coin.getDefaultSymbol(), "0"));
        }
        List<String> newCoinSymbols = coins.stream()
                .filter(coin -> coin.getNewSort() >= 0).sorted((a, b) -> Integer.compare(b.getNewSort(), a.getNewSort()))
                .map(ExchangeCoin::getCoinSymbol).collect(Collectors.toList());
        List<RankDto> rankDtoList = symbolMap.values().stream().map(item -> RankDto.builder()
                .symbol(item.getSymbol())
                .coinSymbol(coinSymbolMap.get(item.getSymbol()))
                .close(item.getClose())
                .volume(item.getVolume())
                .turnover(item.getTurnover())
                .usdAmount((item.getBaseUsdRate()==null?BigDecimal.ZERO:item.getBaseUsdRate()).multiply(item.getTurnover()))
                .chg(item.getChg())
                .build()).collect(Collectors.toList());
        changeRankList = rankDtoList.stream().sorted((a, b) -> b.getChg().compareTo(a.getChg())).collect(Collectors.toList());
        Map<String, RankDto> coinSymbolRankDtoMap = rankDtoList.stream().collect(Collectors.groupingBy(RankDto::getCoinSymbol, Collectors.reducing(null, (a, b) -> {
            if (a == null) {
                return RankDto.builder()
                        .symbol(b.getSymbol())
                        .coinSymbol(b.getCoinSymbol())
                        .close(b.getClose())
                        .volume(b.getVolume())
                        .turnover(b.getTurnover())
                        .usdAmount(b.getUsdAmount())
                        .chg(b.getChg())
                        .build();
            }
            if (!defaultSymbolMap.get(a.getSymbol()) && defaultSymbolMap.get(b.getSymbol())) {
                a.setSymbol(b.getSymbol());
                a.setClose(b.getClose());
                a.setChg(b.getChg());
            }
            a.setVolume(a.getVolume().add(b.getVolume()));
            a.setTurnover(a.getTurnover().add(b.getTurnover()));
            return a;
        })));
        volumeRankList = coinSymbolRankDtoMap.values().stream().sorted((a, b) -> b.getUsdAmount().compareTo(a.getUsdAmount())).collect(Collectors.toList());
        newCoinRankList = newCoinSymbols.stream().map(coinSymbolRankDtoMap::get).collect(Collectors.toList());
    }

    public static CoinRankProcessor getProcessor() {
        return processor;
    }

    public List<RankDto> getChangeRankList(int limit) {
        return getLimitedList(changeRankList, limit);
    }

    public List<RankDto> getVolumeRankList(int limit) {
        return getLimitedList(volumeRankList, limit);
    }

    public List<RankDto> getNewCoinRankList(int limit) {
        return getLimitedList(newCoinRankList, limit);
    }

    private List<RankDto> getLimitedList(List<RankDto> list, int limit) {
        if(list == null){
            return null;
        }
        if(limit < 0){
            return null;
        }
        limit = Math.min(Math.min(limit, list.size()), 100);
        return list.subList(0, limit);
    }

    @Data
    @Builder
    public static class RankDto {
        /**
         * 交易对
         */
        private String symbol;
        /**
         * 币种
         */
        private String coinSymbol;
        /**
         * 当前价
         */
        private BigDecimal close;
        /**
         * 24小时成交量
         */
        private BigDecimal volume;
        /**
         * 24小时成交额
         */
        private BigDecimal turnover;
        /**
         * 折合美元成交额
         */
        private BigDecimal usdAmount;
        /**
         * 涨跌幅
         */
        private BigDecimal chg;
    }
}
