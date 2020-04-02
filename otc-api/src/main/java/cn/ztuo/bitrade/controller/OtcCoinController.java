package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.coin.CoinExchangeFactory;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.OtcCoinService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static cn.ztuo.bitrade.util.MessageResult.success;

/**
 * @author Seven
 * @date 2019年01月06日
 */
@RestController
@Slf4j
@RequestMapping(value = "/coin")
@Api(tags = "otc币种管理")
public class OtcCoinController {

    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private CoinExchangeFactory coins;
    @Autowired
    private CoinService coinService;

    /**
     * 取得正常的币种
     *
     * @return
     */
    @RequestMapping(value = "all")
    @ApiOperation(value = "取得支持法币交易的币种")
    @MultiDataSource(name =  "second")
    public MessageResult allCoin() throws Exception {
        List<Map<String, String>> list = otcCoinService.getAllNormalCoin();
        list.stream().forEachOrdered(x ->{
            if(coins.getCny(x.get("unit")) != null) {
                x.put("marketPrice", coins.getCny(x.get("unit")).setScale(2, RoundingMode.HALF_UP).toPlainString());
            }
            if(coins.getJpy(x.get("unit")) != null) {
                x.put("jpyMarketPrice", coins.getJpy(x.get("unit")).toPlainString());
            }
            Coin coin = coinService.findByUnit(x.get("unit"));
            x.put("imgUrl",coin==null?"":coin.getImgUrl());
        });
        MessageResult result = success();
        result.setData(list);
        return result;
    }
}
