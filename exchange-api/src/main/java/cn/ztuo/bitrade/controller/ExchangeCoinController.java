package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.service.ExchangeCoinService;
import cn.ztuo.bitrade.service.SettlementCoinService;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1816:54
 */
@RestController
@RequestMapping("exchange-coin")
@Api(tags = "币币交易-基础信息")
public class ExchangeCoinController extends BaseController {
    @Autowired
    private ExchangeCoinService service;

    @Autowired
    private SettlementCoinService settlementCoinService;

    @RequestMapping("base-symbol")
    @ApiOperation(value = "获取基币")
    public MessageResult baseSymbol(Integer areaId) {
        List<String> baseSymbol1 = new ArrayList();
        List<String> baseSymbol = settlementCoinService.getBaseSymbol();
        if(areaId!=null && areaId != 0){
            List<String>  baseSymbol2 = service.getBaseSymbolByAreaId(areaId);
            for (String symbol:baseSymbol) {
                if(baseSymbol2.contains(symbol)){
                    baseSymbol1.add(symbol);
                }
            }
        }else{
            return success(baseSymbol);
        }
        if (baseSymbol1 != null && baseSymbol1.size() > 0) {
            return success(baseSymbol1);
        }
        return MessageResult.error(500, msService.getMessage("BASE_SYMBOL_NULL"));
    }

}
