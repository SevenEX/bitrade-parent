package cn.ztuo.bitrade.controller.exchange;

import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.util.*;
import com.alibaba.fastjson.JSON;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.model.screen.ExchangeOrderScreen;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.model.screen.ExchangeTradeScreen;
import cn.ztuo.bitrade.service.ExchangeOrderDetailService;
import cn.ztuo.bitrade.service.ExchangeOrderService;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author MrGao
 * @description
 * @date 2018/1/31 10:52
 */
@Slf4j
@RestController
@RequestMapping("exchange/exchange-order")
@Api(tags = "币币交易订单管理")
public class ExchangeOrderController extends BaseAdminController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Autowired
    private RestTemplate restTemplate;

    @RequiresPermissions(value = {"exchange:exchange-order:page-query","lever:lever-coin:list"}, logical = Logical.OR)
    @PostMapping("all")
   // @AccessLog(module = AdminModule.EXCHANGE, operation = "查找所有exchangeOrder")
    @ApiOperation(value = "查找所有币币交易订单")
    public MessageResult all() {
        List<ExchangeOrder> exchangeOrderList = exchangeOrderService.findAll();
        if (exchangeOrderList != null && exchangeOrderList.size() > 0) {
            return success(exchangeOrderList);
        }
        return error(messageSource.getMessage("NO_DATA"));
    }

    @RequiresPermissions(value = {"exchange:exchange-order:page-query","lever:lever-coin:list"}, logical = Logical.OR)
    @PostMapping("detail")
    //@AccessLog(module = AdminModule.EXCHANGE, operation = "币币交易订单详情")
    @ApiOperation(value = "币币交易订单详情")
    public MessageResult detail(String id) {
        List<ExchangeOrderDetail> one = exchangeOrderService.getAggregation(id);
        ExchangeOrder exchangeOrder=exchangeOrderService.findOne(id);
        if (one == null) {
            return error(messageSource.getMessage("NO_DATA"));
        }
        Map map=new HashMap();
        map.put("detail",one);
        map.put("order",exchangeOrder);
        return success(map);
    }

    @RequiresPermissions(value = {"exchange:exchange-order:page-query","lever:lever-coin:list"}, logical = Logical.OR)
    @PostMapping("page-query")
 //   @AccessLog(module = AdminModule.EXCHANGE, operation = "分页查找币币交易订单")
    @ApiOperation(value = "分页查找币币交易订单")
    public MessageResult page(
            PageModel pageModel,
            ExchangeOrderScreen screen) {
        if (pageModel.getDirection() == null && pageModel.getProperty() == null) {
            ArrayList<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            pageModel.setDirection(directions);
            List<String> property = new ArrayList<>();
            property.add("time");
            pageModel.setProperty(property);
        }
        //获取查询条件
        Predicate predicate = getPredicate(screen);
        Page<ExchangeOrder> all = exchangeOrderService.findAll(predicate, pageModel.getPageable());
        all.getContent().forEach(exchangeOrder->{
            //获取交易成交详情
            BigDecimal tradedAmount = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            exchangeOrder.setDetail(details);
            for(ExchangeOrderDetail trade:details){
                tradedAmount = tradedAmount.add(trade.getAmount());
            }
            exchangeOrder.setTradedAmount(tradedAmount);
        });
        return success(all);
    }

    private Predicate getPredicate(ExchangeOrderScreen screen) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        QExchangeOrder qExchangeOrder = QExchangeOrder.exchangeOrder;
        //booleanExpressions.add(QMember.member.id.eq(QExchangeOrder.exchangeOrder.memberId));
        if (screen.getOrderDirection() != null) {
            booleanExpressions.add(qExchangeOrder.direction.eq(screen.getOrderDirection()));
        }
        if (StringUtils.isNotEmpty(screen.getOrderId())) {
            booleanExpressions.add(qExchangeOrder.orderId.eq(screen.getOrderId()));
        }
        if (screen.getMemberId() != null) {
            booleanExpressions.add(qExchangeOrder.memberId.eq(screen.getMemberId()));
        }
        if (StringUtils.isNotEmpty(screen.getKeyWords())) {
            booleanExpressions.add(qExchangeOrder.memberId.eq(Long.valueOf(screen.getKeyWords())));
        }
        if (screen.getType() != null) {
            booleanExpressions.add(qExchangeOrder.type.eq(screen.getType()));
        }
        if (StringUtils.isNotBlank(screen.getCoinSymbol())) {
            booleanExpressions.add(qExchangeOrder.coinSymbol.equalsIgnoreCase(screen.getCoinSymbol()));
        }
        if (StringUtils.isNotBlank(screen.getSymbol())) {
            booleanExpressions.add(qExchangeOrder.symbol.equalsIgnoreCase(screen.getSymbol()));
        }
        if (StringUtils.isNotBlank(screen.getBaseSymbol())) {
            booleanExpressions.add(qExchangeOrder.baseSymbol.equalsIgnoreCase(screen.getBaseSymbol()));
        }
        if (screen.getStatus() != null) {
            booleanExpressions.add(qExchangeOrder.status.eq(screen.getStatus()));
        }
        if (screen.getMinPrice()!=null) {
            booleanExpressions.add(qExchangeOrder.price.goe(screen.getMinPrice()));
        }
        if (screen.getMaxPrice()!=null) {
            booleanExpressions.add(qExchangeOrder.price.loe(screen.getMaxPrice()));
        }
        if (screen.getMinTradeAmount()!=null) {
            booleanExpressions.add(qExchangeOrder.tradedAmount.goe(screen.getMinTradeAmount()));
        }
        if (screen.getMaxTradeAmount()!=null) {
            booleanExpressions.add(qExchangeOrder.tradedAmount.loe(screen.getMaxTradeAmount()));
        }
        if (screen.getMinTurnOver()!=null) {
            booleanExpressions.add(qExchangeOrder.turnover.goe(screen.getMinTurnOver()));
        }
        if (screen.getMaxTurnOver()!=null) {
            booleanExpressions.add(qExchangeOrder.turnover.loe(screen.getMaxTurnOver()));
        }
        if(screen.getCompleted()!=null) {
            /**
             * 委托订单
             */
            if (screen.getCompleted() == BooleanEnum.IS_FALSE) {
               /* booleanExpressions.add(qExchangeOrder.completedTime.isNull().and(qExchangeOrder.canceledTime.isNull())
                        .and(qExchangeOrder.status.eq(ExchangeOrderStatus.TRADING)));*/
            } else {
                /**
                 * 历史订单
                 */
                booleanExpressions.add(qExchangeOrder.completedTime.isNotNull().or(qExchangeOrder.canceledTime.isNotNull())
                        .or(qExchangeOrder.status.ne(ExchangeOrderStatus.TRADING)));
            }
        }
        if (screen.getStartTime()!=null) {
            booleanExpressions.add(qExchangeOrder.time.goe(screen.getStartTime().getTime()));
        }
        if (screen.getEndTime()!=null) {
            booleanExpressions.add(qExchangeOrder.time.loe(DateUtil.dateAddDay(screen.getEndTime(),1).getTime()));
        }
        if(screen.getMarginTrade()!=null){
            booleanExpressions.add(qExchangeOrder.marginTrade.eq(screen.getMarginTrade()));
        }else{
            booleanExpressions.add(qExchangeOrder.marginTrade.eq(BooleanEnum.IS_FALSE).or(qExchangeOrder.marginTrade.isNull()));
        }
        /*Pattern pattern = Pattern.compile("[0-9]*");
        if (!org.springframework.util.StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            booleanExpressions.add(QMember.member.mobilePhone.like("%" + screen.getKeyWords() + "%")
                    .or(QMember.member.id.eq(Long.valueOf(screen.getKeyWords())))
                    .or(QMember.member.email.like(screen.getKeyWords() + "%")));
        }else if(!org.springframework.util.StringUtils.isEmpty(screen.getKeyWords())){
            booleanExpressions.add(QMember.member.email.like("%" + screen.getKeyWords() + "%"));
        }*/
        return PredicateUtils.getPredicate(booleanExpressions);
    }

    @RequiresPermissions(value = {"exchange:exchange-order:entrust-details","lever:lever-coin:list"}, logical = Logical.OR)
    @PostMapping("entrust-details")
    public MessageResult entrustDetails(ExchangeTradeScreen screen,PageModel pageModel){
       /* ExchangeOrder
        StringBuilder headSql = new StringBuilder("select orderId as IF(a.direction=0,buyOrderId,sellOrderId)");

        StringBuilder headCount = new StringBuilder("select count(*) ");*/
        return  null ;
    }


    @RequiresPermissions(value = {"exchange:exchange-order:out-excel","lever:lever-coin:list"}, logical = Logical.OR)
    @GetMapping("out-excel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "导出币币交易订单")
    @ApiOperation(value = "导出币币交易订单")
    public MessageResult outExcel(
            @RequestParam(value = "memberId") Long memberId,
            @RequestParam(value = "type") ExchangeOrderType type,
            @RequestParam(value = "symbol") String symbol,
            @RequestParam(value = "status") ExchangeOrderStatus status,
            @RequestParam(value = "direction") ExchangeOrderDirection direction,
            @RequestParam(value = "marginTrade",defaultValue = "0")BooleanEnum marginTrade,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取查询条件
        List<Predicate> predicates = getPredicates(memberId, type, symbol, status, direction,marginTrade);
        List list = exchangeOrderService.queryWhereOrPage(predicates, null, null).getContent();
        return new FileUtil().exportExcel(request, response, list, "order");
    }

    //查询条件的获取
    public List<Predicate> getPredicates(Long memberId, ExchangeOrderType type, String symbol, ExchangeOrderStatus status,
                                         ExchangeOrderDirection direction,BooleanEnum marginTrade) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        QExchangeOrder qExchangeOrder = QExchangeOrder.exchangeOrder;
        //predicates.add(qExchangeOrder.symbol.eq(QExchangeCoin.exchangeCoin.symbol));
        if (memberId != null) {
            predicates.add(qExchangeOrder.memberId.eq(memberId));
        }
        if (type != null) {
            predicates.add(qExchangeOrder.type.eq(type));
        }
        if (symbol != null) {
            predicates.add(qExchangeOrder.symbol.eq(symbol));
        }
        if (status != null) {
            predicates.add(qExchangeOrder.status.eq(status));
        }
        if (direction != null) {
            predicates.add(qExchangeOrder.direction.eq(direction));
        }
        if(marginTrade!=null){
            predicates.add(qExchangeOrder.marginTrade.eq(marginTrade));
        }else {
            predicates.add(qExchangeOrder.marginTrade.eq(BooleanEnum.IS_FALSE));
        }
        return predicates;
    }

    @RequiresPermissions(value = {"exchange:exchange-order:page-query","exchange:exchange-order:cancel","lever:lever-coin:list"}, logical = Logical.OR)
    @PostMapping("cancel")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "取消委托")
    @ApiOperation(value = "取消委托订单")
    public MessageResult cancelOrder(String orderId) throws Exception{
        try {
            ExchangeOrder order = exchangeOrderService.findOne(orderId);
            if (order.getStatus() != ExchangeOrderStatus.TRADING) {
                return MessageResult.error(500, "order not in trading");
            }
            // 发送消息至Exchange系统
            if(order.getType()!=ExchangeOrderType.CHECK_FULL_STOP && CheckTraderOrderUtil.isExchangeOrderExist(order,restTemplate)) {
                kafkaTemplate.send("exchange-order-cancel", order.getSymbol(), JSON.toJSONString(order));
            } else if(System.currentTimeMillis() - order.getTime() > 3600 * 1000) {
                exchangeOrderService.forceCancelOrder(order);
            }
            else {
                return MessageResult.error(messageSource.getMessage("INFORMATION_EXPIRED"));
            }
        } catch (Exception e) {
            log.info("取消订单异常={}",e);
            throw new Exception(e);
        }
        return MessageResult.success(messageSource.getMessage("SUCCESS"));
    }
}
