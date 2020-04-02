package cn.ztuo.bitrade.controller.otc;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Advertise;
import cn.ztuo.bitrade.entity.QAdvertise;
import cn.ztuo.bitrade.model.screen.AdvertiseScreen;
import cn.ztuo.bitrade.service.AdvertiseService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.OrderService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.FileUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @author MrGao
 * @description 后台广告web层
 * @date 2018/1/3 9:42
 */
@RestController
@RequestMapping("/otc/advertise")
@Api(tags = "法币交易-广告管理")
public class AdminAdvertiseController extends BaseAdminController {

    @Autowired
    private AdvertiseService advertiseService;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private OrderService orderService;

    @RequiresPermissions("otc:advertise:page-query")
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.OTC, operation = "后台广告Advertise详情")
    @ApiOperation(value = "后台广告Advertise详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(Long id) {
        if (id == null)
            return error("id必传");
        Advertise one = advertiseService.findOne(id);
        if (one == null)
            return error("没有此id的广告");
        return success(messageSource.getMessage("SUCCESS"), one);
    }

    @RequiresPermissions("otc:advertise:page-query")
    @PostMapping("alter-status")
    @AccessLog(module = AdminModule.OTC, operation = "修改广告状态")
    @ApiOperation(value = "修改后台广告Advertise状态")
    public MessageResult statue(
            @RequestParam(value = "ids") Long[] ids,
            @RequestParam(value = "status") AdvertiseControlStatus status) {
        int res=advertiseService.turnOffBatch(status,ids);
        if(res<=0){
            if(res==-100){
                return error("交易中，不允许下架");
            }
            return error(messageSource.getMessage("TURN_OFF_ERROR"));
        }
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("otc:advertise:page-query")
    @PostMapping("page-query")
   // @AccessLog(module = AdminModule.OTC, operation = "分页查找后台广告Advertise")
    @ApiOperation(value = "分页查找后台广告Advertise")
    @MultiDataSource(name = "second")
    public MessageResult page(PageModel pageModel, AdvertiseScreen screen) {
        Predicate predicate = getPredicate(screen);
        Page<Advertise> all = advertiseService.findAll(predicate, pageModel.getPageable());
        all.getContent().forEach(advertise -> {
           String aa = orderService.sumByAdvertiseIdAndStatus(advertise.getId(), OrderStatus.COMPLETED);
            advertise.setDealAmount(StringUtils.isEmpty(aa)?BigDecimal.ZERO : new BigDecimal(aa));
        });
        return success(all);
    }

    @RequiresPermissions("otc:advertise:page-query")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "导出广告")
    @ApiOperation(value = "导出后台广告Advertise")
    @MultiDataSource(name = "second")
    public MessageResult outExcel(
            @RequestParam(value = "startTime", required = false) Date startTime,
            @RequestParam(value = "endTime", required = false) Date endTime,
            @RequestParam(value = "advertiseType", required = false) AdvertiseType advertiseType,
            @RequestParam(value = "realName", required = false) String realName,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<BooleanExpression> booleanExpressionList = getBooleanExpressionList(startTime, endTime, advertiseType, realName);
        List list = advertiseService.queryWhereOrPage(booleanExpressionList, null, null).getContent();
        return new FileUtil().exportExcel(request, response, list, "order");
    }

    // 获得条件
    private List<BooleanExpression> getBooleanExpressionList(
            Date startTime, Date endTime, AdvertiseType advertiseType, String realName) {
        QAdvertise qEntity = QAdvertise.advertise;
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        booleanExpressionList.add(qEntity.status.in(AdvertiseControlStatus.PUT_ON_SHELVES, AdvertiseControlStatus.PUT_OFF_SHELVES));
        if (startTime != null)
            booleanExpressionList.add(qEntity.createTime.gt(startTime));
        if (endTime != null)
            booleanExpressionList.add(qEntity.createTime.lt(endTime));
        if (advertiseType != null)
            booleanExpressionList.add(qEntity.advertiseType.eq(advertiseType));
        if (StringUtils.isNotBlank(realName))
            booleanExpressionList.add(qEntity.member.realName.like("%" + realName + "%"));
        return booleanExpressionList;
    }


    private Predicate getPredicate(AdvertiseScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(screen.getStatus()!=null)
            booleanExpressions.add(QAdvertise.advertise.status.eq(screen.getStatus()));
        if (screen.getAdvertiseType() != null)
            booleanExpressions.add(QAdvertise.advertise.advertiseType.eq(screen.getAdvertiseType()));
        if (screen.getMemberId() != null)
            booleanExpressions.add(QAdvertise.advertise.member.id.eq(screen.getMemberId()));
        if (StringUtils.isNotEmpty(screen.getSymbol()))
            booleanExpressions.add(QAdvertise.advertise.coin.name.like("%"+screen.getSymbol()+"%"));
        if (screen.getStartTime() != null)
            booleanExpressions.add(QAdvertise.advertise.createTime.after(screen.getStartTime()));
        if (screen.getEndTime() != null)
            booleanExpressions.add(QAdvertise.advertise.createTime.before(DateUtil.dateAddDay(screen.getEndTime(),1)));
        /*if (StringUtils.isNotBlank(screen.getAccount()))
            booleanExpressions.add(QAdvertise.advertise.member.realName.like("%" + screen.getAccount() + "%")
                                    .or(QAdvertise.advertise.member.username.like("%" + screen.getAccount() + "%"))
                                    .or(QAdvertise.advertise.member.mobilePhone.like((screen.getAccount()+"%")))
                                    .or(QAdvertise.advertise.member.email.like((screen.getAccount()+"%"))));*/
        if(StringUtils.isNotEmpty(screen.getPayMode()))
            booleanExpressions.add(QAdvertise.advertise.payMode.like("%"+screen.getPayMode()+"%"));
        Pattern pattern = Pattern.compile("[0-9]*");
        if (!org.springframework.util.StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            booleanExpressions.add(QAdvertise.advertise.member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                    .or(QAdvertise.advertise.member.id.eq(Long.valueOf(screen.getKeyWords())))
                    .or(QAdvertise.advertise.member.email.like("%" + screen.getKeyWords() + "%")));
        }else if(!org.springframework.util.StringUtils.isEmpty(screen.getKeyWords())){
            booleanExpressions.add(QAdvertise.advertise.member.email.like("%" + screen.getKeyWords() + "%"));
        }
        return PredicateUtils.getPredicate(booleanExpressions);
    }

    @RequiresPermissions("otc:advertise:page-query")
    @RequestMapping("alter-top")
    @AccessLog(module = AdminModule.OTC, operation = "广告置顶/取消")
    @ApiOperation(value = "广告置顶/取消")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult setTop(
            @RequestParam(value = "id") Long id,
            @RequestParam(value = "top") Integer top) {
        int advertise = advertiseService.alterTopBatch(top,id);
        if(advertise <=0){
            return error("更新失败");
        }
        return success(messageSource.getMessage("SUCCESS"));
    }

}
