package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.Locale;
import cn.ztuo.bitrade.constant.WorkOrderType;
import cn.ztuo.bitrade.entity.Announcement;
import cn.ztuo.bitrade.entity.QAnnouncement;
import cn.ztuo.bitrade.entity.QWorkOrder;
import cn.ztuo.bitrade.entity.WorkOrder;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.LocalizationExtendService;
import cn.ztuo.bitrade.service.MemberService;
import cn.ztuo.bitrade.service.WorkOrderService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.EnumUtils;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

@RestController
@Slf4j
@Api(tags = "工单")
@RequestMapping("/workOrder")
public class WorkOrderController  extends BaseController{
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private LocalizationExtendService localizationExtendService;
    /**
     * 提交工单
     * @return
     */
    @RequestMapping(value = "add",method = RequestMethod.POST)
    @ApiOperation(value = "提交工单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "description", value = "描述", required = true, dataType = "String"),
            @ApiImplicitParam(name = "imgUrl", value = "图片地址", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "分类（0、BUG优化 1、功能优化 2、新增功能）", required = true, dataType = "String"),
            @ApiImplicitParam(name = "contact", value = "联系方式", required = true, dataType = "String"),
    })
    @Transactional(rollbackFor = Exception.class)
    public MessageResult workOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, WorkOrder workOrder) {
        workOrder.setMemberId(user.getId());
        workOrder.setCreateTime(DateUtil.getCurrentDate());
        workOrder.setStatus(CommonStatus.NORMAL);
        workOrderService.save(workOrder);
        return MessageResult.success();
    }

    @PostMapping("page")
    @ApiOperation(value = "分页获取工单列表")
    @MultiDataSource(name = "second")
    public MessageResult page(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        //条件
        ArrayList<Predicate> predicates = new ArrayList<>();
        predicates.add(QWorkOrder.workOrder.memberId.eq(user.getId()));
        //排序
        ArrayList<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        orderSpecifiers.add(QWorkOrder.workOrder.createTime.desc());
        //查
        PageResult<WorkOrder> pageResult = workOrderService.queryDsl(pageNo, pageSize, predicates, QWorkOrder.workOrder, orderSpecifiers);
        return success(pageResult);
    }

    @GetMapping("getType")
    @ApiOperation(value = "获取工单类型")
    @MultiDataSource(name = "second")
    public MessageResult getType() {
        return success(EnumUtils.getEnumInfo(WorkOrderType.class));
    }
}
