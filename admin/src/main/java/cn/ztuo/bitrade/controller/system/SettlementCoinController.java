package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.SettlementCoin;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.SettlementCoinService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("/system/settlementCoin")
@Slf4j
@Api(tags = "结算币种管理")
public class SettlementCoinController extends BaseAdminController {
    @Autowired
    private CoinService coinService;

    @Autowired
    private SettlementCoinService settlementCoinService;

    @PostMapping("allSettlement")
   // @AccessLog(module = AdminModule.COIN, operation = "查询所有结算币种")
    @ApiOperation(value = "查询所有结算币种")
    @MultiDataSource(name = "second")
    public MessageResult findAllSettlement() {
        List<Coin> coinList = coinService.findAllByStatusAndIsSettlement(CommonStatus.NORMAL);
        MessageResult result = MessageResult.success();
        result.setData(coinList);
        return result;
    }

    @PostMapping("allCoin")
    //@AccessLog(module = AdminModule.COIN, operation = "查询所有非结算币种")
    @ApiOperation(value = "查询所有非结算币种")
    @MultiDataSource(name = "second")
    public MessageResult findAllNotSettlement() {
        List<Coin> coinList = coinService.findAllByStatusAndIsSettlementNot(CommonStatus.NORMAL);
        MessageResult result = MessageResult.success();
        result.setData(coinList);
        return result;
    }

    @RequiresPermissions("system:settlement")
    @PostMapping("page-query")
   // @AccessLog(module = AdminModule.COIN, operation = "分页查找结算币种")
    @ApiOperation(value = "分页查找结算币种")
    @MultiDataSource(name = "second")
    public MessageResult settlementPageQuery(PageModel pageModel,String coinName,CommonStatus status) {
        Page<SettlementCoin> pageResult = settlementCoinService.findAll(pageModel,coinName,status);
        return success(pageResult);
    }

    @RequiresPermissions("system:settlement")
    @PostMapping("add")
    @AccessLog(module = AdminModule.COIN, operation = "新增结算币种")
    @ApiOperation(value = "新增结算币种")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addSettlement(@Valid SettlementCoin settlementCoin, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        Coin one = coinService.findOne(settlementCoin.getCoinName());
        notNull(one, "validate coin.name!");
        one.setIsSettlement(true);
        settlementCoin.setCreateTime(DateUtil.getCurrentDate());
        settlementCoinService.save(settlementCoin);
        coinService.save(one);
        return success(settlementCoin);
    }

    @RequiresPermissions("system:settlement")
    @PostMapping("update")
    @AccessLog(module = AdminModule.COIN, operation = "编辑结算币种")
    @ApiOperation(value = "编辑结算币种")
    public MessageResult updateSettlement(@Valid SettlementCoin settlementCoin, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
       // SettlementCoin one = settlementCoinService
        settlementCoinService.save(settlementCoin);
        return success(settlementCoin);
    }

    @RequiresPermissions("system:settlement")
    @PostMapping("delete")
    @AccessLog(module = AdminModule.COIN, operation = "删除结算币种")
    @ApiOperation(value = "删除结算币种")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deleteSettlement(String coinName) {
        Coin one = coinService.findOne(coinName);
        notNull(one, "validate coin.name!");
        one.setIsSettlement(false);
        settlementCoinService.deleteOne(coinName);
        coinService.save(one);
        return success();
    }
}
