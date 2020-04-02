package cn.ztuo.bitrade.controller.otc;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.OtcCoin;
import cn.ztuo.bitrade.entity.QOtcCoin;
import cn.ztuo.bitrade.service.CoinService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.OtcCoinService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.FileUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.isNull;
import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description otc币种
 * @date 2018/1/11 13:35
 */
@RestController
@RequestMapping("/otc/otc-coin")
@Api(tags = "法币交易-币种管理")
public class AdminOtcCoinController extends BaseAdminController {

    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private CoinService coinService ;

    @RequiresPermissions("otc:otc-coin:page-query")
    @PostMapping("create")
    @AccessLog(module = AdminModule.OTC, operation = "创建otc币种")
    @ApiOperation(value = "创建otc币种")
    public MessageResult create(@Valid OtcCoin otcCoin, BindingResult bindingResult) {
        isNull(otcCoin.getId(), "validate otcCoin.id!");
        OtcCoin oldCoin=otcCoinService.findByUnit(otcCoin.getUnit());
        if(oldCoin!=null){
            return MessageResult.error(messageSource.getMessage("PRE_COIN_EXIST"));
        }
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        Coin coin = coinService.findByUnit(otcCoin.getUnit());
        if(coin==null)
            return error(messageSource.getMessage("COIN_NOT_SUPPORTED"));
        otcCoin.setName(otcCoin.getUnit());
        otcCoinService.save(otcCoin);
        return success();
    }

    @RequiresPermissions("otc:otc-coin:page-query")
    @PostMapping("all")
    //@AccessLog(module = AdminModule.OTC, operation = "所有otc币种otcCoin")
    @ApiOperation(value = "所有otc币种")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<OtcCoin> all = otcCoinService.findAll();
        if (all != null && all.size() > 0)
            return success(all);
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("otc:otc-coin:page-query")
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.OTC, operation = "otc币种otcCoin详情")
    @ApiOperation(value = "币种详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam("id") Long id) {
        OtcCoin one = otcCoinService.findOne(id);
        notNull(one, "validate otcCoin.id!");
        return success(one);
    }

    @RequiresPermissions("otc:otc-coin:page-query")
    @PostMapping("update")
    @AccessLog(module = AdminModule.OTC, operation = "更新otc币种")
    @ApiOperation(value = "更新币种")
    public MessageResult update(@Valid OtcCoin otcCoin, BindingResult bindingResult) {
        notNull(otcCoin.getId(), "validate otcCoin.id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        OtcCoin one = otcCoinService.findOne(otcCoin.getId());
        notNull(one, "validate otcCoin.id!");
        otcCoinService.save(otcCoin);
        return success();
    }

    @RequiresPermissions("otc:otc-coin:page-query")
    @PostMapping("deletes")
    @AccessLog(module = AdminModule.OTC, operation = "otc币种删除")
    @ApiOperation(value = "删除币种")
    public MessageResult deletes(
            @RequestParam(value = "ids") Long[] ids) {
        otcCoinService.deletes(ids);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("otc:otc-coin:page-query")
    @PostMapping("alter-jy-rate")
    @AccessLog(module = AdminModule.OTC, operation = "修改otc币种otcCoin交易手续费率")
    @ApiOperation(value = "修改币种交易手续费率")
    public MessageResult memberStatistics(
            @RequestParam("id") Long id,
            @RequestParam("jyRate") BigDecimal jyRate) {
        OtcCoin one = otcCoinService.findOne(id);
        notNull(one, "validate otcCoin.id");
        one.setJyRate(jyRate);
        otcCoinService.save(one);
        return success();
    }

    @RequiresPermissions("otc:otc-coin:page-query")
    @PostMapping("page-query")
   // @AccessLog(module = AdminModule.OTC, operation = "分页查找otc币种otcCoin")
    @ApiOperation(value = "分页查找币种")
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(PageModel pageModel, String name, CommonStatus status) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(status!=null)
            booleanExpressions.add(QOtcCoin.otcCoin.status.eq(status));
        if(!StringUtils.isEmpty(name))
            booleanExpressions.add(QOtcCoin.otcCoin.name.like("%"+name+"%"));
        Page<OtcCoin> pageResult = otcCoinService.findAll(PredicateUtils.getPredicate(booleanExpressions), pageModel.getPageable());
        return success(pageResult);
    }

    @RequiresPermissions("otc:otc-coin:page-query")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "导出otc币种")
    @ApiOperation(value = "导出otc币种")
    @MultiDataSource(name = "second")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = otcCoinService.findAll();
        return new FileUtil().exportExcel(request, response, all, "otcCoin");
    }

    @PostMapping("all-otc-coin-units")
    @ApiOperation(value = "获取所有可交易币种")
    @MultiDataSource(name = "second")
    public MessageResult getAllOtcCoinUnits(){
        List<String> list = otcCoinService.findAllUnits() ;
        return success(messageSource.getMessage("SUCCESS"),list);
    }
}
