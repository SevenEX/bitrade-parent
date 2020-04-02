package cn.ztuo.bitrade.job;

import cn.ztuo.bitrade.coin.CoinExchangeFactory;
import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.core.DataException;
import cn.ztuo.bitrade.entity.OtcCoin;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.service.AdvertiseService;
import cn.ztuo.bitrade.service.OtcCoinService;
import cn.ztuo.bitrade.util.RedissonUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Seven
 * @date 2019年02月01日
 */
@Component
@Slf4j
public class CheckAdvertiseTask {
    @Autowired
    private CoinExchangeFactory coins;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private AdvertiseService advertiseService;

//    @Scheduled(fixedRate = 5*60*1000)
//    public void checkExpireOrder() throws InterruptedException {
    @XxlJob("checkExpireAd")
    public ReturnT<String> checkExpireAd(String param) throws Exception {
        RLock fairLock = RedissonUtil.getFairLock(RedissonKeyConstant.JOB_ADVERTISE_AUTO_OFF);
        if (!fairLock.tryLock(0, 60, TimeUnit.SECONDS)){
//            return;
            return ReturnT.SUCCESS;
        }
        log.info("=========开始检查自动下架的广告===========");
        //支持的币种
        List<OtcCoin> list = otcCoinService.getNormalCoin();
        Map<String, BigDecimal> map = coins.getCnyCoins();
        list.stream().forEach(
            x -> {
                BigDecimal marketPrice = map.get(x.getUnit());
                try {
                    List<Map<String, String>> list1 = advertiseService.selectSellAutoOffShelves(x.getId(), marketPrice, x.getJyRate());
                    List<Map<String, String>> list2 = advertiseService.selectBuyAutoOffShelves(x.getId(), marketPrice);
                    list1.addAll(list2);
                    list1.stream().forEach(
                            y -> {
                                try {
                                    advertiseService.autoPutOffShelves(y, x);
                                } catch (InformationExpiredException e) {
                                    e.printStackTrace();
                                    log.warn("{}号广告:自动下架失败", y.get("id"));
                                }
                            }
                    );
                } catch (SQLException e) {
                    log.warn("{}号广告:自动下架失败",e);
                }catch (DataException e2){
                    log.warn("{}号广告:自动下架失败",e2);
                }
            }
        );
        log.info("=========结束检查自动下架的广告===========");
        return ReturnT.SUCCESS;
    }
}
