package cn.ztuo.bitrade.controller.statistics;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.statistics.*;
import cn.ztuo.bitrade.service.StatisticsService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.ztuo.bitrade.entity.statistics.QStatisticsRecharge.statisticsRecharge;
import static cn.ztuo.bitrade.entity.statistics.QStatisticsServiceFee.statisticsServiceFee;
import static cn.ztuo.bitrade.entity.statistics.QStatisticsTransaction.statisticsTransaction;
import static cn.ztuo.bitrade.entity.statistics.QStatisticsWithdraw.statisticsWithdraw;

/**
 * @author Paradise
 */
@RestController
@RequestMapping("/statistics")
@Api(tags = "数据统计")
public class StatisticsHomeController extends BaseAdminController {
    private final StatisticsService statisticsService;

    public StatisticsHomeController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequiresPermissions("statistics:register:page-query")
    @PostMapping("/registerPageQuery")
    @ApiOperation("分页查询数据统计-注册统计")
    @MultiDataSource(name = "second")
    public MessageResult registerPageQuery(Date startDate, Date endDate,
                                           PageModel pageModel) {
        dealPageModel(pageModel);
        Page<StatisticsRegister> page = statisticsService.findAllStatisticsRegister(startDate, endDate,
                pageModel.getPageable());
        return success(page);
    }

    @RequiresPermissions("statistics:recharge:page-query")
    @PostMapping("/rechargePageQuery")
    @ApiOperation("分页查询数据统计-充币统计")
    @MultiDataSource(name = "second")
    public MessageResult rechargePageQuery(Date startDate, Date endDate,
                                           String currency, PageModel pageModel) {
        ArrayList<BooleanExpression> l = new ArrayList<>();
        if (StringUtils.isNotBlank(currency)) {
            l.add(statisticsRecharge.currency.eq(currency));
        }
        if (endDate != null && startDate != null) {
            l.add(statisticsRecharge.date.lt(DateUtil.dateAddDay(endDate, 1)));
            l.add(statisticsRecharge.date.goe(startDate));
        }
        dealPageModel(pageModel);
        Page<StatisticsRecharge> page = statisticsService.findAllStatisticsRecharge(PredicateUtils.getPredicate(l),
                pageModel.getPageable());
        return success(page);
    }

    @RequiresPermissions("statistics:withdrawal:page-query")
    @PostMapping("/withdrawPageQuery")
    @ApiOperation("分页查询数据统计-提币统计")
    @MultiDataSource(name = "second")
    public MessageResult withdrawPageQuery(Date startDate, Date endDate,
                                           String currency, PageModel pageModel) {
        List<BooleanExpression> s = new ArrayList<>();
        if (StringUtils.isNotBlank(currency)) {
            s.add(statisticsWithdraw.currency.eq(currency));
        }
        if (startDate != null && endDate != null) {
            s.add(statisticsWithdraw.date.lt(DateUtil.dateAddDay(endDate, 1)));
            s.add(statisticsWithdraw.date.goe(startDate));
        }
        dealPageModel(pageModel);
        Page<StatisticsWithdraw> page = statisticsService.findAllStatisticsWithdraw(PredicateUtils.getPredicate(s),
                pageModel.getPageable());
        return success(page);
    }

    @RequiresPermissions("statistics:transaction:page-query")
    @PostMapping("/transactionPageQuery")
    @ApiOperation("分页查询数据统计-交易统计")
    @MultiDataSource(name = "second")
    public MessageResult transactionPageQuery(Date startDate, Date endDate, String symbol, PageModel pageModel) {
        ArrayList<BooleanExpression> l = new ArrayList<>();
        if (StringUtils.isNotBlank(symbol)) {
            l.add(statisticsTransaction.symbol.eq(symbol));
        }
        if (startDate != null && endDate != null) {
            l.add(statisticsTransaction.date.lt(DateUtil.dateAddDay(endDate, 1)));
            l.add(statisticsTransaction.date.goe(startDate));
        }
        dealPageModel(pageModel);
        Page<StatisticsTransaction> page = statisticsService.findAllStatisticsTransaction(PredicateUtils.getPredicate(l),
                pageModel.getPageable());
        return success(page);
    }

    @RequiresPermissions("statistics:fee:page-query")
    @PostMapping("/serviceFeePageQuery")
    @ApiOperation("分页查询数据统计-手续费统计")
    @MultiDataSource(name = "second")
    public MessageResult serviceFeePageQuery(Date startDate, Date endDate, String currency, PageModel pageModel) {
        ArrayList<BooleanExpression> l = new ArrayList<>();
        if (startDate != null && endDate != null) {
            l.add(statisticsServiceFee.date.goe(startDate));
            l.add(statisticsServiceFee.date.lt(DateUtil.dateAddDay(endDate, 1)));
        }
        if (StringUtils.isNotBlank(currency)) {
            l.add(statisticsServiceFee.currency.eq(currency));
        }
        dealPageModel(pageModel);
        Page<StatisticsServiceFee> page =
                statisticsService.findAllStatisticsServiceFee(PredicateUtils.getPredicate(l),
                        pageModel.getPageable());
        return success(page);
    }

    private void dealPageModel(PageModel pageModel) {
        if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            list.add("date");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
    }

    @RequiresPermissions("statistics:recharge:page-query")
    @PostMapping("/rechargeSumQuery")
    @ApiOperation("汇总查询数据统计-充币统计")
    @MultiDataSource(name = "second")
    public MessageResult rechargeSumQuery(String startDate, String endDate, String currency) {
        if (StringUtils.isEmpty(currency)) {
            return MessageResult.error("币种为空");
        }
        return success(statisticsService.rechargeSumQuery(startDate, endDate, currency));
    }

    @RequiresPermissions("statistics:withdrawal:page-query")
    @PostMapping("/withdrawSumQuery")
    @ApiOperation("汇总查询数据统计-提币统计")
    @MultiDataSource(name = "second")
    public MessageResult withdrawSumQuery(String startDate, String endDate, String currency) {
        if (StringUtils.isEmpty(currency)) {
            return MessageResult.error("币种为空");
        }
        return success(statisticsService.withdrawSumQuery(startDate, endDate, currency));
    }

    @RequiresPermissions("statistics:register:page-query")
    @PostMapping("/registerSumQuery")
    @ApiOperation("汇总查询数据统计-注册统计")
    @MultiDataSource(name = "second")
    public MessageResult registerSumQuery(String startDate, String endDate) {
        return success(statisticsService.registerSumQuery(startDate, endDate));
    }

    @RequiresPermissions("statistics:transaction:page-query")
    @PostMapping("/transactionSumQuery")
    @ApiOperation("汇总查询数据统计-交易统计")
    @MultiDataSource(name = "second")
    public MessageResult transactionSumQuery(String startDate, String endDate, String symbol) {
        if (StringUtils.isEmpty(symbol)) {
            return MessageResult.error("币对为空");
        }
        return success(statisticsService.transactionSumQuery(startDate, endDate, symbol));
    }

    @RequiresPermissions("statistics:fee:page-query")
    @PostMapping("/serviceFeeSumQuery")
    @ApiOperation("汇总查询数据统计-手续费")
    @MultiDataSource(name = "second")
    public MessageResult serviceFeeSumQuery(String startDate, String endDate, String currency) {
        if (StringUtils.isEmpty(currency)) {
            return MessageResult.error("币种为空");
        }
        return success(statisticsService.serviceFeeSumQuery(startDate, endDate, currency));
    }
}
