package cn.ztuo.bitrade.controller.loan;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.entity.*;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.enums.PerformActionsEnum;
import cn.ztuo.bitrade.service.LeverCoinService;
import cn.ztuo.bitrade.service.LoanRecordService;
import cn.ztuo.bitrade.service.LossThresholdService;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 管理杠杆交易亏损阈值
 */
@Slf4j
@RestController
@RequestMapping("/loss_threshold")
@Api(tags = "杠杆交易亏损阈值（暂弃）")
public class LossThresholdController {
    @Autowired
    private LossThresholdService lossThresholdService;
    @Autowired
    private LeverCoinService leverCoinService;
    @Autowired
    private LoanRecordService loanRecordService;

    /**
     * 查询所有的亏损阈值
     * @return
     */
    @RequiresPermissions("loss-threshold:all")
    @RequestMapping(value = "list",method = RequestMethod.POST)
   // @AccessLog(module = AdminModule.MARGIN, operation = "查询所有的亏损阈值")
    @ApiOperation(value = "查询所有的亏损阈值")
    @MultiDataSource(name = "second")
    public MessageResult listLossThreshold(@RequestParam("pageNum") Integer pageNum,@RequestParam("pageSize")Integer pageSize,BooleanEnum booleanEnum){
        Sort sort = Sort.by(Sort.Direction.DESC,"id");

        PageRequest pageRequest = PageRequest.of(pageNum-1,pageSize,sort);
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(booleanEnum!=null){
            booleanExpressions.add(QLeverCoin.leverCoin.enable.eq(booleanEnum));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<LeverCoin> page=leverCoinService.findAll(predicate,pageRequest);
        List<LeverCoin> leverCoinList = page.getContent();
        MessageResult result=MessageResult.success();
        if(leverCoinList!=null&&leverCoinList.size()>0){
            List<LossThreshold> lossThresholdList=lossThresholdService.getAll();
            result.setData(lossThresholdList);
        }
        result.setTotal(page.getTotalElements());
        return result;
    }

    /**
     * 创建亏损阈值
     * @param leverCoinSymbol
     * @param threshold
     * @param performActions
     * @return
     */
    @RequiresPermissions("loss-threshold:create")
    @RequestMapping(value = "create",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.MARGIN, operation = "创建亏损阈值")
    @ApiOperation(value = "创建亏损阈值")
    public MessageResult create(@RequestParam("coinUnit") String leverCoinSymbol, @RequestParam("threshold")BigDecimal threshold,
                                @RequestParam("performActions")PerformActionsEnum performActions){
        Assert.isTrue(leverCoinSymbol!=null&&threshold!=null&&performActions!=null,"缺少必要参数");
        if(threshold.compareTo(new BigDecimal("100"))<=0){
            return MessageResult.error("风险率必须高于100%");
        }
        LeverCoin leverCoin=leverCoinService.getBySymbol(leverCoinSymbol);
        Assert.notNull(leverCoin,"leverCoinSymbol不存在");
        LossThreshold oldData=lossThresholdService.findByLeverCoinAndPerformActions(leverCoin,performActions);
        Assert.isNull(oldData,leverCoinSymbol+"已经设置过这种类型的阈值");
        LossThreshold lossThreshold=new LossThreshold();
        lossThreshold.setLeverCoin(leverCoin);
        lossThreshold.setPerformActions(performActions);
        lossThreshold.setThreshold(threshold);
        lossThreshold.setCreateTime(new Date());
        lossThresholdService.save(lossThreshold);
        return MessageResult.success();
    }

    /**
     * 修改亏损阈值
     * @param id
     * @param threshold
     * @param status
     * @return
     */
    @RequiresPermissions("lossThreshold:update")
    @RequestMapping(value = "update",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.MARGIN, operation = "修改亏损阈值")
    @ApiOperation(value = "修改亏损阈值")
    public MessageResult update(@RequestParam("id")Long id, BigDecimal threshold,
                                CommonStatus status){
        Assert.notNull(id,"缺少必要参数");
        LossThreshold lossThreshold=lossThresholdService.findById(id);
        Assert.notNull(lossThreshold,"参数有误");
        if(threshold!=null){
            if(threshold.compareTo(new BigDecimal(100))<=0){
                return MessageResult.error("风险率必须高于100%");
            }
            lossThreshold.setThreshold(threshold);
        }

        if(status!=null){
            lossThreshold.setStatus(status);
        }
        lossThreshold.setUpdateTime(new Date());
        lossThresholdService.save(lossThreshold);
        return MessageResult.success();
    }

    /**
     * 删除亏损阈值
     * @param id
     * @return
     */
    @RequiresPermissions("lossThreshold:delete")
    @RequestMapping(value = "delete",method = RequestMethod.POST)
    @AccessLog(module = AdminModule.MARGIN, operation = "删除亏损阈值")
    @ApiOperation(value = "删除亏损阈值")
    public MessageResult delete(@RequestParam("id")Long id){
        Assert.notNull(id,"缺少参数");
        lossThresholdService.deleteById(id);
        return MessageResult.success();
    }

    /**
     * 查询借贷记录
     *
     * @param pageModel
     * @return
     */
    @AccessLog(module = AdminModule.MARGIN, operation = "查询借贷记录")
    @RequiresPermissions("lossThreshold:record")
    @RequestMapping(value = "record",method = RequestMethod.POST)
    @ApiOperation(value = "查询借贷记录")
    @MultiDataSource(name = "second")
    public MessageResult record(String userName,String symbol,BooleanEnum repayment, PageModel pageModel){
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(StringUtils.isNotBlank(userName)){
            booleanExpressions.add(QLoanRecord.loanRecord.memberName.like(userName));
        }
        if(StringUtils.isNotBlank(symbol)){
            booleanExpressions.add(QLoanRecord.loanRecord.leverCoin.symbol.eq(symbol));
        }
        if(repayment!=null){
            booleanExpressions.add(QLoanRecord.loanRecord.repayment.eq(repayment));
        }
        Predicate predicate= PredicateUtils.getPredicate(booleanExpressions);
        Page<LoanRecord> loanRecordList=loanRecordService.findAll(predicate,pageModel);
        MessageResult result=MessageResult.success();
        result.setData(loanRecordList);
        return result;
    }
}
