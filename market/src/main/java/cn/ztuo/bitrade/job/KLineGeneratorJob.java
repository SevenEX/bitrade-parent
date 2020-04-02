package cn.ztuo.bitrade.job;

import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.processor.CoinProcessorFactory;
import cn.ztuo.bitrade.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * 生成各时间段的K线信息
 */
@Component
@Slf4j
public class KLineGeneratorJob {
    @Autowired
    private CoinProcessorFactory processorFactory;

    /**
     * 每分钟定时器，处理分钟K线
     */
    @Scheduled(cron = "0 * * * * *")
    public void handle5minKLine() throws InterruptedException {
        Calendar calendar = Calendar.getInstance();
        log.info("分钟K线:{}", calendar.getTime());
        //将秒、微秒字段置为0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        RLock fairLock = RedissonUtil.getFairLock(RedissonKeyConstant.GENERATE_MIN_KLINE + time);
        if (!fairLock.tryLock(0, 30, TimeUnit.SECONDS)){
            if (hour == 0 && minute == 0) {
                processorFactory.getProcessorMap().forEach((symbol, processor) -> {
                    //更新24H成交量
                    processor.update24HVolume(time);
                    processor.resetThumb();
                });
            }
            return;
        }
        processorFactory.getProcessorMap().forEach((symbol, processor) -> {
            log.info("生成{}分钟k线:{}", symbol);
            //生成1分钟K线
            processor.autoGenerate();
            //更新24H成交量
            processor.update24HVolume(time);
            if (minute % 5 == 0) {
                processor.generateKLine(5, Calendar.MINUTE, time);
            }
            if (minute % 10 == 0) {
                processor.generateKLine(10, Calendar.MINUTE, time);
            }
            if (minute % 15 == 0) {
                processor.generateKLine(15, Calendar.MINUTE, time);
            }
            if (minute % 30 == 0) {
                processor.generateKLine(30, Calendar.MINUTE, time);
            }
            if (hour == 0 && minute == 0) {
                processor.resetThumb();
            }
        });
    }

    /**
     * 每小时运行
     */
    @Scheduled(cron = "0 0 * * * *")
    public void handleHourKLine() throws InterruptedException {
        Calendar calendar = Calendar.getInstance();
        log.info("小时K线:{}", calendar.getTime());
        //将秒、微秒字段置为0
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        RLock fairLock = RedissonUtil.getFairLock(RedissonKeyConstant.GENERATE_HOUR_KLINE + time);
        if (!fairLock.tryLock(0, 30, TimeUnit.MINUTES))
            return;
        processorFactory.getProcessorMap().forEach((symbol, processor) -> {
            processor.generateKLine(1, Calendar.HOUR_OF_DAY, time);
        });
    }

    /**
     * 每日0点处理器，处理日K线
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void handleDayKLine() throws InterruptedException {
        Calendar calendar = Calendar.getInstance();
        log.info("日K线:{}", calendar.getTime());
        //将秒、微秒字段置为0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();
        RLock fairLock = RedissonUtil.getFairLock(RedissonKeyConstant.GENERATE_DAY_KLINE + time);
        if (!fairLock.tryLock(0, 12, TimeUnit.HOURS))
            return;
        processorFactory.getProcessorMap().forEach((symbol, processor) -> {
            int week = calendar.get(Calendar.DAY_OF_WEEK);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            if (week == 1) {
                processor.generateKLine(1, Calendar.DAY_OF_WEEK, time);
            }
            if (dayOfMonth == 1) {
                processor.generateKLine(1, Calendar.DAY_OF_MONTH, time);
            }
            processor.generateKLine(1, Calendar.DAY_OF_YEAR, time);
        });
    }


}
