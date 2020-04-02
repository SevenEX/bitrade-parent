package cn.ztuo.bitrade.controller.otc;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.model.screen.OrderScreen;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.ExcelUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.OrderStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.BaseController;
import cn.ztuo.bitrade.model.screen.OrderScreen;
import cn.ztuo.bitrade.entity.Order;
import cn.ztuo.bitrade.entity.QOrder;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.service.OrderService;
import cn.ztuo.bitrade.vo.OtcOrderVO;
import cn.ztuo.bitrade.model.screen.OrderScreen;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 法币交易订单
 * @date 2018/1/8 15:41
 */
@RestController
@RequestMapping("/otc/order")
@Api(tags = "法币交易-交易订单管理")
public class AdminOrderController extends BaseController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("otc:order:page-query")
    @PostMapping("all")
   // @AccessLog(module = AdminModule.OTC, operation = "所有法币交易订单Order")
    @ApiOperation(value = "所有法币交易订单Order")
    @MultiDataSource(name = "second")
    public MessageResult all() {
        List<Order> exchangeOrderList = orderService.findAll();
        if (exchangeOrderList != null && exchangeOrderList.size() > 0)
            return success(exchangeOrderList);
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions("otc:order:page-query")
    @PostMapping("detail")
   // @AccessLog(module = AdminModule.OTC, operation = "法币交易订单Order详情")
    @ApiOperation(value = "法币交易订单Order详情")
    public MessageResult detail(Long id) {
        Order one = orderService.findOne(id);
        if (one == null)
            return error(messageSource.getMessage("NO_DATA"));
        return success(one);
    }

    //修改订单状态
    @RequiresPermissions("otc:order:page-query")
    @PatchMapping("{id}/alert-status")
    @AccessLog(module = AdminModule.OTC, operation = "修改订单状态")
    @ApiOperation(value = "修改法币交易订单Order")
    public MessageResult status(
            @PathVariable("id") Long id,
            @RequestParam("status") OrderStatus status) {
        Order order = orderService.findOne(id);
        notNull(order, "validate order.id!");
        order.setStatus(status);
        orderService.save(order);
        return success();
    }


    @RequiresPermissions(value = {"otc:order:page-query","finance:otc:order:page-query"},logical = Logical.OR)
    @PostMapping("page-query")
    //@AccessLog(module = AdminModule.OTC, operation = "分页查找法币交易订单Order")
    @ApiOperation(value = "分页查找法币交易订单Order")
    @MultiDataSource(name = "second")
    public MessageResult page(
            PageModel pageModel,
            OrderScreen screen) {
        List<Predicate> predicate = getPredicates(screen);
        Page<OtcOrderVO> page = orderService.outExcel(predicate,pageModel);
        //Page<Order> all = orderService.findAll(predicate, pageModel.getPageable());
        return success(page);
    }

    private List<Predicate> getPredicates(OrderScreen screen) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        //predicates.add(QOrder.order.status.ne(OrderStatus.CANCELLED));
        if (StringUtils.isNotBlank(screen.getOrderSn()))
            predicates.add(QOrder.order.orderSn.eq(screen.getOrderSn()));
        if (screen.getStartTime() != null)
            predicates.add(QOrder.order.createTime.goe(screen.getStartTime()));
        if (screen.getEndTime() != null){
            predicates.add(QOrder.order.createTime.lt(DateUtil.dateAddDay(screen.getEndTime(),1)));
        }
        if (screen.getStatus() != null)
            predicates.add(QOrder.order.status.eq(screen.getStatus()));
        if (StringUtils.isNotEmpty(screen.getUnit()))
            predicates.add(QOrder.order.coin.unit.equalsIgnoreCase(screen.getUnit()));
       /* if (StringUtils.isNotBlank(screen.getMemberName()))
            predicates.add(QOrder.order.memberName.like("%" + screen.getMemberName() + "%")
                                    .or(QOrder.order.memberRealName.like("%" + screen.getMemberName() + "%")));
        if (StringUtils.isNotBlank(screen.getCustomerName()))
            predicates.add(QOrder.order.customerName.like("%" + screen.getCustomerName() + "%")
                                    .or(QOrder.order.customerRealName.like("%" + screen.getCustomerName() + "%")));*/
        if (StringUtils.isNotEmpty(screen.getKeyWords()))
            predicates.add(QOrder.order.memberId.eq(Long.valueOf(screen.getKeyWords()))
                    .or(QOrder.order.customerId.eq(Long.valueOf(screen.getKeyWords()))));
       if (screen.getMemberId()!=null)
            predicates.add(QOrder.order.memberId.eq(screen.getMemberId())
                    .or(QOrder.order.customerId.eq(screen.getMemberId())));
        if(screen.getMinMoney()!=null)
            predicates.add(QOrder.order.money.goe(screen.getMinMoney()));
        if(screen.getMaxMoney()!=null)
            predicates.add(QOrder.order.money.loe(screen.getMaxMoney()));
        if(screen.getMinNumber()!=null)
            predicates.add(QOrder.order.number.goe(screen.getMinNumber()));
        if(screen.getMaxNumber()!=null)
            predicates.add(QOrder.order.number.loe(screen.getMaxNumber()));
        if(screen.getAdvertiseType()!=null){
            predicates.add(QOrder.order.advertiseType.eq(screen.getAdvertiseType()));
        }
        if(screen.getPayModel()!=null)
            predicates.add(QOrder.order.payMode.like("%"+screen.getPayModel()+"%"));
        return /*PredicateUtils.getPredicate(booleanExpressions)*/predicates;
    }


    @RequiresPermissions("otc:order:page-query")
    @PostMapping("get-order-num")
  //  @AccessLog(module = AdminModule.OTC, operation = "后台首页订单总数接口")
    @ApiOperation(value = "后台首页订单总数接口")
    @MultiDataSource(name = "second")
    public MessageResult getOrderNum() {
        return orderService.getOrderNum();
    }

    /**
     * 参数 fileName 为导出excel 文件的文件名 格式为 .xls  定义在OutExcelInterceptor 拦截器中 ，非必须参数
     * @param pageModel
     * @param screen
     * @param response
     * @throws Exception
     */
    @RequiresPermissions("otc:order:page-query")
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.OTC, operation = "导出法币交易订单")
    @ApiOperation(value = "导出法币交易订单Order")
    @MultiDataSource(name = "second")
    public void outExcel(
            PageModel pageModel,
            OrderScreen screen,
            HttpServletResponse response
            ) throws Exception {
        List<OtcOrderVO> list = orderService.outExcel(getPredicates(screen),pageModel).getContent();
        ExcelUtil.listToExcel(list,OtcOrderVO.class.getDeclaredFields(),response.getOutputStream());
    }


}
