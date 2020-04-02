package cn.ztuo.bitrade.controller.cms;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.WorkOrder;
import cn.ztuo.bitrade.service.WorkOrderService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.FileUtil;
import cn.ztuo.bitrade.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("/cms/work-order")
@Api(tags = "工单管理")
public class WorkOrderController extends BaseAdminController {

    @Autowired
    private WorkOrderService workOrderService;

    @RequiresPermissions("cms:work-order:page-query")
    @PostMapping("/create")
    @AccessLog(module = AdminModule.WORKORDER, operation = "创建工单")
    @ApiOperation(value = "创建工单")
    public MessageResult create(@Valid WorkOrder workOrder, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        workOrder.setCreateTime(DateUtil.getCurrentDate());
        workOrder = workOrderService.save(workOrder);
        return success(workOrder);
    }

    @RequiresPermissions("cms:work-order:page-query")
    @PostMapping("/all")
  //  @AccessLog(module = AdminModule.WORKORDER, operation = "查找所有工单")
    @ApiOperation(value = "查找所有工单")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<WorkOrder> workOrders = workOrderService.findAll();
        if (workOrders != null && workOrders.size() > 0)
            return success(workOrders);
        return error("data null");
    }

    @RequiresPermissions("cms:work-order:page-query")
    @PostMapping("/detail")
   // @AccessLog(module = AdminModule.WORKORDER, operation = "工单详情")
    @ApiOperation(value = "工单详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        WorkOrder workOrder = workOrderService.findOne(id);
        notNull(workOrder, "validate id!");
        return success(workOrder);
    }

    @RequiresPermissions("cms:work-order:page-query")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.WORKORDER, operation = "更新工单")
    @ApiOperation(value = "更新工单")
    public MessageResult update(@Valid WorkOrder workOrder, BindingResult bindingResult) {
        notNull(workOrder.getId(), "validate id!");
        if(workOrder.getStatus() == CommonStatus.ILLEGAL){
            notNull(workOrder.getDetail(), "请录入处理方式和处理过程!");
            workOrder.setDetail(workOrder.getDetail());
            workOrder.setReplyTime(DateUtil.getCurrentDate());
        }
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null)
            return result;
        WorkOrder one = workOrderService.findOne(workOrder.getId());
        notNull(one, "validate id!");
        workOrder.setCreateTime(one.getCreateTime());
        workOrderService.save(workOrder);
        return success(workOrder);
    }

    @RequiresPermissions("cms:work-order:page-query")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.WORKORDER, operation = "删除工单")
    @ApiOperation(value = "删除工单")
    public MessageResult deleteOne(@RequestParam("ids") Long[] ids) {
        workOrderService.deleteBatch(ids);
        return success();
    }

    @RequiresPermissions("cms:work-order:page-query")
    @PostMapping("/page-query")
    //   @AccessLog(module = AdminModule.WORKORDER, operation = "分页查询工单")
    @ApiOperation(value = "分页查询工单")
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(PageModel pageModel, WorkOrder workOrder, String keyWords) {
        Page<WorkOrder> all = workOrderService.findByCondition(pageModel.getPageNo(), pageModel.getPageSize(), workOrder, keyWords);
        return success(all);
    }

    @RequiresPermissions("cms:work-order:page-query")
    @GetMapping("/out-excel")
    @AccessLog(module = AdminModule.WORKORDER, operation = "导出工单Excel")
    @ApiOperation(value = "导出工单Excel")
    @MultiDataSource(name = "second")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = workOrderService.findAll();
        return new FileUtil().exportExcel(request, response, all, "workOrder");
    }
}
