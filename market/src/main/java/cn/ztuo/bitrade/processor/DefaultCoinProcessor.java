package cn.ztuo.bitrade.processor;


import cn.ztuo.bitrade.entity.CoinThumb;
import cn.ztuo.bitrade.util.DateUtil;
import com.alibaba.fastjson.JSON;
import cn.ztuo.bitrade.component.CoinExchangeRate;
import cn.ztuo.bitrade.entity.ExchangeTrade;
import cn.ztuo.bitrade.entity.KLine;
import cn.ztuo.bitrade.handler.MarketHandler;
import cn.ztuo.bitrade.service.MarketService;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 默认交易处理器，产生1mK线信息
 */
@ToString
public class DefaultCoinProcessor implements CoinProcessor {
    private Logger logger = LoggerFactory.getLogger(DefaultCoinProcessor.class);
    private String symbol;
    private String baseCoin;
    private KLine currentKLine;
    private KLine currentKLine5m;
    private KLine currentKLine15m;
    private KLine currentKLine30m;
    private KLine currentKLine1h;
    private KLine currentKLine1d;
    private KLine currentKLine1w;
    private KLine currentKLine1M;
    private List<MarketHandler> handlers;
    private CoinThumb coinThumb;
    private MarketService service;
    private CoinExchangeRate coinExchangeRate;
    //是否暂时处理
    private Boolean isHalt = true;
    private int baseScale;
    private int coinScale;

    public DefaultCoinProcessor(String symbol, String baseCoin) {
        handlers = new ArrayList<>();
        createNewKLine();
        createNewKLine("5min");
        createNewKLine("15min");
        createNewKLine("30min");
        createNewKLine("1hour");
        createNewKLine("1day");
        createNewKLine("1week");
        createNewKLine("1month");
        this.baseCoin = baseCoin;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public void initializeThumb() {
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long nowTime = calendar.getTimeInMillis();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long firstTimeOfToday = calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long firstTimeOfMonth = calendar.getTimeInMillis();
        String period = "1min";
        logger.info("initializeThumb from {} to {}", firstTimeOfToday, nowTime);
        List<KLine> lines = service.findAllKLine(this.symbol, firstTimeOfToday, nowTime, period);
        List<KLine> dayLines = service.findAllKLine(this.symbol, firstTimeOfMonth, nowTime, "1day");
        coinThumb = new CoinThumb();
        synchronized (coinThumb) {
            coinThumb.setSymbol(symbol);
            for (KLine kline : lines) {
                if (kline.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) == 0) {
                    coinThumb.setOpen(kline.getOpenPrice());
                }
                if (coinThumb.getHigh().compareTo(kline.getHighestPrice()) < 0) {
                    coinThumb.setHigh(kline.getHighestPrice());
                }
                if (kline.getLowestPrice().compareTo(BigDecimal.ZERO) > 0 && coinThumb.getLow().compareTo(kline.getLowestPrice()) > 0) {
                    coinThumb.setLow(kline.getLowestPrice());
                }
                if (kline.getClosePrice().compareTo(BigDecimal.ZERO) > 0) {
                    coinThumb.setClose(kline.getClosePrice());
                }
                coinThumb.setVolume(coinThumb.getVolume().add(kline.getVolume()));
                coinThumb.setTurnover(coinThumb.getTurnover().add(kline.getTurnover()));
            }
            coinThumb.setChange(coinThumb.getClose().subtract(coinThumb.getOpen()));
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(coinThumb.getChange().divide(coinThumb.getOpen(), 4, RoundingMode.UP));
            }
            coinThumb.setCloseStr(coinThumb.getClose().setScale(baseScale, RoundingMode.DOWN).toPlainString());
            try {
                BigDecimal cnyPrice = coinThumb.getClose().multiply(coinThumb.getBaseUsdRate()).multiply(BigDecimal.valueOf(6.8)).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                coinThumb.setCnyPrice(cnyPrice == null ? "0" : cnyPrice.toPlainString());
            } catch (Exception e) {
                logger.info("初始化价格为空");
            }
        }
        prepareLines(lines, dayLines);
    }

    private void prepareLines(List<KLine> lines, List<KLine> dayLines) {
        Calendar calendar = Calendar.getInstance();
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int min = (calendar.get(Calendar.MINUTE) / 5) * 5;
        calendar.set(Calendar.MINUTE, min);
        long last5Min = calendar.getTimeInMillis();
        min = (calendar.get(Calendar.MINUTE) / 15) * 15;
        calendar.set(Calendar.MINUTE, min);
        long last15Min = calendar.getTimeInMillis();
        min = (calendar.get(Calendar.MINUTE) / 30) * 30;
        calendar.set(Calendar.MINUTE, min);
        long last30Min = calendar.getTimeInMillis();
        calendar.set(Calendar.MINUTE, 0);
        long lastHour = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long lastWeek = DateUtil.getThisWeekMonday(calendar.getTime()).getTime();
        currentKLine5m = lines.stream().filter(line -> line.getTime() >= last5Min).sorted(Comparator.comparing(KLine::getTime)).reduce(currentKLine5m, (lineA, lineB) -> {
            processKLine(lineA, lineB);
            return lineA;
        });
        currentKLine15m = lines.stream().filter(line -> line.getTime() >= last15Min).sorted(Comparator.comparing(KLine::getTime)).reduce(currentKLine15m, (lineA, lineB) -> {
            processKLine(lineA, lineB);
            return lineA;
        });
        currentKLine30m = lines.stream().filter(line -> line.getTime() >= last30Min).sorted(Comparator.comparing(KLine::getTime)).reduce(currentKLine30m, (lineA, lineB) -> {
            processKLine(lineA, lineB);
            return lineA;
        });
        currentKLine1h = lines.stream().filter(line -> line.getTime() >= lastHour).sorted(Comparator.comparing(KLine::getTime)).reduce(currentKLine1h, (lineA, lineB) -> {
            processKLine(lineA, lineB);
            return lineA;
        });
        currentKLine1d = lines.stream().sorted(Comparator.comparing(KLine::getTime)).reduce(currentKLine1d, (lineA, lineB) -> {
            processKLine(lineA, lineB);
            return lineA;
        });
        currentKLine1w = Stream.concat(lines.stream(), dayLines.stream()).filter(line -> line.getTime() >= lastWeek).sorted(Comparator.comparing(KLine::getTime)).reduce(currentKLine1w, (lineA, lineB) -> {
            processKLine(lineA, lineB);
            return lineA;
        });
        currentKLine1M = Stream.concat(lines.stream(), dayLines.stream()).sorted(Comparator.comparing(KLine::getTime)).reduce(currentKLine1M, (lineA, lineB) -> {
            processKLine(lineA, lineB);
            return lineA;
        });
    }

    public void createNewKLine() {
        currentKLine = new KLine();
        synchronized (currentKLine) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            //1Min时间要是下一整分钟的
            calendar.add(Calendar.MINUTE, 1);
            currentKLine.setTime(calendar.getTimeInMillis());
            currentKLine.setPeriod("1min");
            currentKLine.setCount(0);
        }
    }

    private void createNewKLine(String period) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        KLine kline = new KLine();
        kline.setPeriod(period);
        kline.setCount(0);
        switch (period) {
            case "5min":
                int min = (calendar.get(Calendar.MINUTE) / 5 + 1) * 5;
                if (min == 60) {
                    min = 0;
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
                calendar.set(Calendar.MINUTE, min);
                kline.setTime(calendar.getTimeInMillis());
                currentKLine5m = kline;
                break;
            case "15min":
                min = (calendar.get(Calendar.MINUTE) / 15 + 1) * 15;
                if (min == 60) {
                    min = 0;
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
                calendar.set(Calendar.MINUTE, min);
                kline.setTime(calendar.getTimeInMillis());
                currentKLine15m = kline;
                break;
            case "30min":
                min = (calendar.get(Calendar.MINUTE) / 30 + 1) * 30;
                if (min == 60) {
                    min = 0;
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
                calendar.set(Calendar.MINUTE, min);
                kline.setTime(calendar.getTimeInMillis());
                currentKLine30m = kline;
                break;
            case "1hour":
                calendar.set(Calendar.MINUTE, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                kline.setTime(calendar.getTimeInMillis());
                currentKLine1h = kline;
                break;
            case "1day":
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                kline.setTime(calendar.getTimeInMillis());
                currentKLine1d = kline;
                break;
            case "1week":
                kline.setTime(DateUtil.getNextWeekMonday(calendar.getTime()).getTime());
                currentKLine1w = kline;
                break;
            case "1month":
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, 1);
                kline.setTime(calendar.getTimeInMillis());
                currentKLine1M = kline;
                break;
            default:
                break;
        }
    }

    /**
     * 00:00:00 时重置CoinThumb
     */
    @Override
    public void resetThumb() {
        logger.info("reset coinThumb");
        synchronized (coinThumb) {
            Calendar calendar = Calendar.getInstance();
            //将秒、微秒字段置为0
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long time = calendar.getTimeInMillis();
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            long timeEnd = calendar.getTimeInMillis();
            List<ExchangeTrade> trades = this.service.findTradeByTimeRange(coinThumb.getSymbol(), time, timeEnd);
            coinThumb.setOpen(trades.stream().map(ExchangeTrade::getPrice).findFirst().orElse(BigDecimal.ZERO));
            List<BigDecimal> prices = trades.stream().map(ExchangeTrade::getPrice).sorted().collect(Collectors.toList());
            coinThumb.setHigh(prices.size() > 0 ? prices.get(0) : BigDecimal.ZERO);
            coinThumb.setLow(prices.size() > 0 ? prices.get(prices.size() - 1) : BigDecimal.ZERO);
            //设置昨收价格
            coinThumb.setClose(trades.size() > 0 ? trades.get(trades.size() - 1).getPrice() : coinThumb.getClose());
            coinThumb.setChange(coinThumb.getClose().subtract(coinThumb.getOpen()));
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(coinThumb.getChange().divide(coinThumb.getOpen(), 4, BigDecimal.ROUND_UP));
            } else {
                coinThumb.setChg(BigDecimal.ZERO);
            }
        }
    }

    @Override
    public void setExchangeRate(CoinExchangeRate coinExchangeRate) {
        this.coinExchangeRate = coinExchangeRate;
    }

    @Override
    public void update24HVolume(long time) {
        if (coinThumb != null) {
            synchronized (coinThumb) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);
                calendar.add(Calendar.HOUR_OF_DAY, -24);
                long timeStart = calendar.getTimeInMillis();
                BigDecimal volume = service.findTradeVolume(this.symbol, timeStart, time);
                coinThumb.setVolume(volume.setScale(4, RoundingMode.DOWN));
            }
        }
    }

    @Override
    public void initializeUsdRate() {
        logger.info("symbol = {} ,baseCoin = {}", this.symbol, this.baseCoin);
        BigDecimal baseUsdRate = coinExchangeRate.getCoinLegalRate("USD", baseCoin);
        coinThumb.setBaseUsdRate(baseUsdRate == null ? BigDecimal.ZERO : baseUsdRate);
        logger.info("setBaseUsdRate = ", baseUsdRate);
        BigDecimal multiply = coinThumb.getClose().multiply(baseUsdRate == null ? BigDecimal.ZERO : baseUsdRate);
        logger.info("setUsdRate = ", multiply);
        coinThumb.setUsdRate(multiply == null ? BigDecimal.ZERO : multiply);
    }


    @Override
    public String getBaseCoin() {
        return this.baseCoin;
    }

    @Override
    public void setScale(int coinScale, int baseCoinScale) {
        this.coinScale = coinScale;
        this.baseScale = baseCoinScale;
    }

    @Override
    public void autoGenerate() {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        logger.info("auto generate 1min kline in {},data={}", df.format(new Date(currentKLine.getTime())), JSON.toJSONString(currentKLine));
        if (coinThumb != null) {
            synchronized (currentKLine) {
                //没有成交价时存储上一笔成交价
                if (currentKLine.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                    currentKLine.setOpenPrice(coinThumb.getClose());
                    currentKLine.setLowestPrice(coinThumb.getClose());
                    currentKLine.setHighestPrice(coinThumb.getClose());
                    currentKLine.setClosePrice(coinThumb.getClose());
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                currentKLine.setTime(calendar.getTimeInMillis());
                handleKLineStorage(currentKLine);
                handleKLinePush(currentKLine5m);
                handleKLinePush(currentKLine15m);
                handleKLinePush(currentKLine30m);
                handleKLinePush(currentKLine1h);
                handleKLinePush(currentKLine1d);
                handleKLinePush(currentKLine1w);
                handleKLinePush(currentKLine1M);
                createNewKLine();
            }
        }
    }

    @Override
    public void setIsHalt(boolean status) {
        this.isHalt = status;
    }

    @Override
    public void process(List<ExchangeTrade> trades) {
        if (!isHalt) {
            if (trades == null || trades.size() == 0) {
                return;
            }
            synchronized (currentKLine) {
                for (ExchangeTrade exchangeTrade : trades) {
                    //处理K线
                    processTrade(currentKLine, exchangeTrade);
                    //处理今日概况信息
                    logger.info("处理今日概况信息");
                    handleThumb(exchangeTrade);
                    //存储并推送成交信息
                    handleTradeStorage(exchangeTrade);
                }
                processKLine(currentKLine5m, currentKLine);
                processKLine(currentKLine15m, currentKLine);
                processKLine(currentKLine30m, currentKLine);
                processKLine(currentKLine1h, currentKLine);
                processKLine(currentKLine1d, currentKLine);
                processKLine(currentKLine1w, currentKLine);
                processKLine(currentKLine1M, currentKLine);
                handleKLinePush(currentKLine);
                handleKLinePush(currentKLine5m);
                handleKLinePush(currentKLine15m);
                handleKLinePush(currentKLine30m);
                handleKLinePush(currentKLine1h);
                handleKLinePush(currentKLine1d);
                handleKLinePush(currentKLine1w);
                handleKLinePush(currentKLine1M);
            }
        }
    }

    private void processKLine(KLine kLine, KLine currentKLine) {
        synchronized (kLine) {
            if (kLine.getClosePrice().compareTo(BigDecimal.ZERO) == 0) {
                //第一次设置K线值
                kLine.setOpenPrice(currentKLine.getOpenPrice());
                kLine.setHighestPrice(currentKLine.getHighestPrice());
                kLine.setLowestPrice(currentKLine.getLowestPrice());
                kLine.setClosePrice(currentKLine.getClosePrice());
            } else {
                kLine.setHighestPrice(currentKLine.getHighestPrice().max(kLine.getHighestPrice()));
                kLine.setLowestPrice(currentKLine.getLowestPrice().min(kLine.getLowestPrice()));
                kLine.setClosePrice(currentKLine.getClosePrice());
            }
            kLine.setCount(kLine.getCount() + currentKLine.getCount());
            kLine.setVolume(kLine.getVolume().add(currentKLine.getVolume()));
            kLine.setTurnover(kLine.getTurnover().add(currentKLine.getTurnover()));
        }
    }

    public void processTrade(KLine kLine, ExchangeTrade exchangeTrade) {
        if (kLine.getClosePrice().compareTo(BigDecimal.ZERO) == 0) {
            //第一次设置K线值
            kLine.setOpenPrice(exchangeTrade.getPrice());
            kLine.setHighestPrice(exchangeTrade.getPrice());
            kLine.setLowestPrice(exchangeTrade.getPrice());
            kLine.setClosePrice(exchangeTrade.getPrice());
        } else {
            kLine.setHighestPrice(exchangeTrade.getPrice().max(kLine.getHighestPrice()));
            kLine.setLowestPrice(exchangeTrade.getPrice().min(kLine.getLowestPrice()));
            kLine.setClosePrice(exchangeTrade.getPrice());
        }
        kLine.setCount(kLine.getCount() + 1);
        kLine.setVolume(kLine.getVolume().add(exchangeTrade.getAmount()));
        BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount());
        kLine.setTurnover(kLine.getTurnover().add(turnover));
    }

    public void handleTradeStorage(ExchangeTrade exchangeTrade) {
        for (MarketHandler storage : handlers) {
            exchangeTrade.setAmountStr(exchangeTrade.getAmount().setScale(coinScale, RoundingMode.DOWN).toPlainString());
            exchangeTrade.setPriceStr(exchangeTrade.getPrice().setScale(baseScale, RoundingMode.DOWN).toPlainString());
            storage.handleTrade(symbol, exchangeTrade, coinThumb);
        }
    }

    public void handleKLineStorage(KLine kLine) {
        for (MarketHandler storage : handlers) {
            storage.handleKLine(symbol, kLine);
        }
    }

    public void handleKLinePush(KLine kLine) {
        for (MarketHandler storage : handlers) {
            if (!storage.isPersistent()) {
                storage.handleKLine(symbol, kLine);
            }
        }
    }

    public void handleThumb(ExchangeTrade exchangeTrade) {
        logger.info("handleThumb symbol = {}", this.symbol);
        synchronized (coinThumb) {
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) == 0) {
                //第一笔交易记为开盘价
                coinThumb.setOpen(exchangeTrade.getPrice());
            }
            coinThumb.setHigh(exchangeTrade.getPrice().max(coinThumb.getHigh()));
            if (coinThumb.getLow().compareTo(BigDecimal.ZERO) == 0) {
                coinThumb.setLow(exchangeTrade.getPrice());
            } else {
                coinThumb.setLow(exchangeTrade.getPrice().min(coinThumb.getLow()));
            }
            coinThumb.setClose(exchangeTrade.getPrice());
            coinThumb.setVolume(coinThumb.getVolume().add(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP));
            BigDecimal turnover = exchangeTrade.getPrice().multiply(exchangeTrade.getAmount()).setScale(4, RoundingMode.UP);
            coinThumb.setTurnover(coinThumb.getTurnover().add(turnover));
            BigDecimal change = coinThumb.getClose().subtract(coinThumb.getOpen());
            coinThumb.setChange(change);
            if (coinThumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
                coinThumb.setChg(change.divide(coinThumb.getOpen(), 4, BigDecimal.ROUND_UP));
            }
            coinThumb.setBaseUsdRate(coinExchangeRate.getCoinLegalRate("USD", baseCoin));
            coinThumb.setUsdRate(exchangeTrade.getPrice().multiply(coinThumb.getBaseUsdRate()));
            coinThumb.setCloseStr(coinThumb.getClose().setScale(baseScale, RoundingMode.DOWN).toPlainString());
            try {
                BigDecimal cnyPrice = coinThumb.getClose().multiply(coinThumb.getBaseUsdRate()).multiply(BigDecimal.valueOf(6.8)).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                coinThumb.setCnyPrice(cnyPrice == null ? "0" : cnyPrice.toPlainString());

            } catch (Exception e) {
                logger.info("初始化价格为空");
            }
            logger.info("thumb = {}", coinThumb);
        }
    }

    @Override
    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }

    @Override
    public CoinThumb getThumb() {
        return coinThumb;
    }

    @Override
    public void setMarketService(MarketService service) {
        this.service = service;
    }

    @Override
    public void generateKLine(int range, int field, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long endTick = calendar.getTimeInMillis();
        String endTime = df.format(calendar.getTime());
        //往前推range个时间单位
        calendar.add(field, -range);
        String fromTime = df.format(calendar.getTime());
        long startTick = calendar.getTimeInMillis();
        logger.info("time range from " + fromTime + " to " + endTime);
        List<ExchangeTrade> exchangeTrades = service.findTradeByTimeRange(this.symbol, startTick, endTick);
        KLine kLine = new KLine();
        //k线的time值设置为起始时刻
        kLine.setTime(startTick);
        String rangeUnit = "";
        if (field == Calendar.MINUTE) {
            rangeUnit = "min";
        } else if (field == Calendar.HOUR_OF_DAY) {
            rangeUnit = "hour";
        } else if (field == Calendar.DAY_OF_WEEK) {
            rangeUnit = "week";
        } else if (field == Calendar.DAY_OF_YEAR) {
            rangeUnit = "day";
        } else if (field == Calendar.MONTH) {
            rangeUnit = "month";
        }
        kLine.setPeriod(range + rangeUnit);

        // 处理K线信息
        for (ExchangeTrade exchangeTrade : exchangeTrades) {
            processTrade(kLine, exchangeTrade);
        }
        service.saveKLine(symbol, kLine);
        createNewKLine(kLine.getPeriod());
    }

    @Override
    public KLine getKLine() {
        return currentKLine;
    }
}
