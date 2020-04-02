package cn.ztuo.bitrade.controller;


import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.entity.AssetExchangeCoin;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.AssetExchangeService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.List;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

/**
 * 币种兑换服务，按预先设计好的汇率相互对换
 */
@RestController
@RequestMapping("/exchange")
@Api(tags = "币种兑换")
public class CoinExchangeController extends BaseController{
    @Autowired
    private AssetExchangeService assetExchangeService;

    /**
     * 获取支持兑换的币种
     * @param unit
     * @return
     */
    @RequestMapping(value = "supported-coin",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取支持兑换的币种")
    @MultiDataSource(name = "second")
    public List<AssetExchangeCoin> findSupportedCoin(String unit){
        return assetExchangeService.findAllByFromCoin(unit);
    }

    /**
     * c
     * @param member
     * @param from
     * @param to
     * @param amount
     */
    @RequestMapping(value = "transfer",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "币种兑换")
    public MessageResult exchange(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, String from, String to, BigDecimal amount){
        AssetExchangeCoin coin =  assetExchangeService.findOne(from,to);
        if(coin == null){
            return new MessageResult(500,msService.getMessage("COIN_ILLEGAL"));
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            return new MessageResult(500,msService.getMessage("EXCHANGE_NUMBER_MIN"));
        }
        return assetExchangeService.exchange(member.getId(),coin,amount);
    }
}
