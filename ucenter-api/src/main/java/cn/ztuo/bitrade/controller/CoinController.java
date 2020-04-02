package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.LocalizationExtendService;
import cn.ztuo.bitrade.service.LocalizationService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MrGao
 * @Description: coin
 * @date 2018/4/214:20
 */
@RestController
@RequestMapping("coin")
@Api(tags = "币种管理")
public class CoinController extends BaseController {
    @Autowired
    private CoinService coinService;

    @Autowired
    private LocalizationExtendService localizationExtendService;

    @GetMapping("legal")
    @ApiOperation(value = "查询所有法币币种")
    @MultiDataSource(name = "second")
    public MessageResult legal() {
        List<Coin> legalAll = coinService.findLegalAll();
        return success(legalAll);
    }

    @GetMapping("legal/page")
    @ApiOperation(value = "分页获取法币币种")
    @MultiDataSource(name = "second")
    public MessageResult findLegalCoinPage(PageModel pageModel) {
        Page all = coinService.findLegalCoinPage(pageModel);
        return success(all);
    }

    @RequestMapping(value = "supported",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取币种列表")
    @MultiDataSource(name = "second")
    public List<Map<String,Object>>  findCoins(){
        // 被禁用的币种不显示 start
//        List<Coin> coins = coinService.findAll();
        List<Coin> coins = coinService.findAllByStatus(CommonStatus.NORMAL);
        // 被禁用的币种不显示 end
        List<Map<String,Object>> result = new ArrayList<>();
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        coins.forEach(coin->{
            if(coin.getHasLegal().equals(Boolean.FALSE)) {
                String cnName = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "name");
                String description = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "description");
                String depositMessage = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "depositMessage");
                String withdrawMessage = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "withdrawMessage");
                Map<String, Object> map = new HashMap<>();
                map.put("name", coin.getName());
                map.put("nameCn", StringUtils.firstNonBlank(cnName, coin.getName()));
                map.put("withdrawFee",String.valueOf(coin.getMinTxFee()));
                map.put("enableRecharge",String.valueOf(coin.getCanRecharge().getOrdinal()));
                map.put("minWithdrawAmount",String.valueOf(coin.getMinWithdrawAmount()));
                map.put("enableWithdraw",String.valueOf(coin.getCanWithdraw().getOrdinal()));
                map.put("maxWithdrawAmount",coin.getMaxWithdrawAmount().toPlainString());
                map.put("withdrawThreshold",coin.getWithdrawThreshold().toPlainString());
                map.put("imgUrl",coin.getImgUrl());
                map.put("releaseAmount",coin.getReleaseAmount());
                map.put("releaseTime",coin.getReleaseTime());
                map.put("fundPrice",coin.getFundPrice());
                map.put("whitePaper",coin.getWhitePaper());
                map.put("website",coin.getWebsite());
                map.put("blockQuery",coin.getBlockQuery());
                map.put("description",description);
                map.put("depositMessage",depositMessage);
                map.put("withdrawMessage",withdrawMessage);
                map.put("chainInfo", coin.getCoinChainRelationList());
                map.put("masterAddress", coin.getMasterAddress());
                map.put("withdrawScale", coin.getWithdrawScale());
                result.add(map);
            }
        });
        return result;
    }

    @RequestMapping(value = "coinDetail",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取币种详情")
    @MultiDataSource(name = "second")
    public MessageResult  findCoinDetail(String name){
        Coin coin = coinService.findOne(name);
        if(coin == null){
            return error(msService.getMessage("NO_COIN"));
        }
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        String cnName = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "name");
        String description = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "description");
        Map<String, Object> map = new HashMap<>();
        map.put("coin", coin);
        map.put("nameCn", StringUtils.firstNonBlank(cnName, coin.getName()));
        map.put("description",description);
        return success(map);
    }

    @RequestMapping(value = "coinExplain",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "充提币规则/手续费说明")
    @MultiDataSource(name = "second")
    public MessageResult  coinExplain(String name){
        Coin coin = coinService.findOne(name);
        if(coin == null){
            return error(msService.getMessage("NO_COIN"));
        }
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        String cnName = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "name");
        String depositMessage = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "depositMessage");
        String withdrawMessage = localizationExtendService.getLocaleInfo("Coin", locale, coin.getName(), "withdrawMessage");        Map<String, Object> map = new HashMap<>();
        map.put("coin", coin);
        map.put("nameCn", StringUtils.firstNonBlank(cnName, coin.getName()));
        map.put("depositMessage",depositMessage);
        map.put("withdrawMessage",withdrawMessage);
        return success(map);
    }
}
