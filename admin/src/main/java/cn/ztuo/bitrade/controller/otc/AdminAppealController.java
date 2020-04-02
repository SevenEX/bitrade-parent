package cn.ztuo.bitrade.controller.otc;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.event.OrderEvent;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.model.screen.AppealScreen;
import cn.ztuo.bitrade.remind.RemindService;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.AppealVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.ztuo.bitrade.util.BigDecimalUtils.add;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 后台申诉管理
 * @date 2018/1/23 9:26
 */
@RestController
@RequestMapping("/otc/appeal")
@Api(tags = "法币交易-申诉管理")
public class AdminAppealController extends BaseAdminController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdvertiseService advertiseService;

    @Autowired
    private OtcWalletService otcWalletService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private OrderEvent orderEvent;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private RemindService remindService;

    @RequiresPermissions("otc:appeal:page-query")
    @PostMapping("page-query")
    // @AccessLog(module = AdminModule.OTC, operation = "分页查找后台申诉Appeal")
    @ApiOperation(value = "分页查找后台申诉Appeal")
    @MultiDataSource(name = "second")
    public MessageResult pageQuery(
            PageModel pageModel,
            AppealScreen screen) {

        StringBuilder headSqlBuilder = new StringBuilder("select a.id appealId,a.type type,a.img_urls imgUrls,a.initiator_id initiatorId,")
                .append("b.price price,b.reference_number referenceNumber,b.advertise_id advertiseId,b.member_id memberId,")
                .append("b.member_name advertiseCreaterUserName,b.member_real_name advertiseCreaterName,")
                .append("b.customer_id customerId,b.customer_name customerUserName,b.customer_real_name customerName,")
                .append("c.username initiatorUsername,c.real_name initiatorName,")
                .append("d.username associateUsername,d.real_name associateName,")
                .append("b.commission fee,b.number,b.money,b.order_sn orderSn,b.create_time transactionTime,")
                .append("a.create_time createTime,a.deal_with_time dealWithTime,b.pay_mode payMode, e.name coinName,")
                .append("b.status orderStatus,a.is_success isSuccess,b.advertise_type advertiseType,a.status,a.remark ");

        StringBuilder countHead = new StringBuilder("select count(*) ");

        StringBuilder endSql = new StringBuilder("from appeal a,otc_order b,member c,member d,otc_coin e")
                .append(" where a.order_id = b.id and a.initiator_id = c.id and a.associate_id = d.id ")
                .append(" and b.coin_id = e.id ");

        if (!StringUtils.isEmpty(screen.getNegotiant()))
            endSql.append(" and (b.customer_name like '%" + screen.getNegotiant() + "%'")
                    .append(" or b.customer_real_name like '%" + screen.getNegotiant() + "%')");
        if (!StringUtils.isEmpty(screen.getComplainant()))
            endSql.append(" and (b.member_name like '%" + screen.getComplainant() + "%'")
                    .append(" or b.member_real_name like '%" + screen.getComplainant() + "%')");

        if (screen.getAdvertiseType() != null)
            endSql.append(" and b.advertise_type = " + screen.getAdvertiseType().getOrdinal() + " ");

        /*if (screen.getMemberId() != null)
            endSql.append(" and (a.initiator_id = '"+screen.getMemberId()+"' or a.associate_id = '"+screen.getMemberId()+"') ");*/
        if (screen.getMemberId() != null)
            endSql.append(" and a.initiator_id = '" + screen.getMemberId() + "' ");
        if (screen.getStatus() != null)
            endSql.append(" and a.status = " + screen.getStatus().getOrdinal() + " ");
        try {
            if (screen.getStartTime() != null) {
                    endSql.append(" and a.create_time >= '" + DateUtil.getFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),screen.getStartTime()) + "' ");
            }
            if (screen.getEndTime() != null)
                endSql.append(" and a.create_time <= '" + DateUtil.getFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),DateUtil.dateAddDay(screen.getEndTime(), 1)) + "' ");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (screen.getSuccess() != null) {
            endSql.append(" and (a.is_success = " + screen.getSuccess().getOrdinal() + " and a.deal_with_time is not null) ");
        } else {
            if (screen.getAuditing()) {
                endSql.append(" and a.is_success is null ");
            }
        }

        if (!StringUtils.isEmpty(screen.getUnit()))
            endSql.append(" and lower(e.unit) = '" + screen.getUnit().toLowerCase() + "'");

        if (screen.getOrderStatus() != null && screen.getOrderStatus().getOrdinal() != 0)
            endSql.append(" and b.status = " + screen.getOrderStatus().getOrdinal());

        Pattern pattern = Pattern.compile("[0-9]*");
        if (!org.springframework.util.StringUtils.isEmpty(screen.getKeyWords())&&pattern.matcher(screen.getKeyWords()).matches()) {
            endSql.append(" and (c.id = " + screen.getKeyWords() + " or c.mobile_phone like '%" + screen.getKeyWords() + "%' or c.email like '%" + screen.getKeyWords() + "%' " +
                    "or d.id = " + screen.getKeyWords() + " or d.mobile_phone like '%" + screen.getKeyWords() + "%' or d.email like '%" + screen.getKeyWords() + "%' )");
        }else if(!org.springframework.util.StringUtils.isEmpty(screen.getKeyWords())){
            endSql.append(" and (c.email like '%" + screen.getKeyWords() + "%' " +
                    " or d.email like '%" + screen.getKeyWords() + "%' )");
        }
        ArrayList<Sort.Direction> directions = new ArrayList<>();
        directions.add(Sort.Direction.DESC);
        pageModel.setDirection(directions);
        List<String> property = new ArrayList<>();
        property.add("a.create_time");
        pageModel.setProperty(property);
        Page page = appealService.createNativePageQuery(countHead.append(endSql), headSqlBuilder.append(endSql.append(" ")), pageModel, Transformers.ALIAS_TO_ENTITY_MAP);

        return success("获取成功", page);
    }

    @RequiresPermissions("otc:appeal:page-query")
    @PostMapping("detail")
    // @AccessLog(module = AdminModule.OTC, operation = "后台申诉Appeal详情")
    @ApiOperation(value = "后台申诉Appeal详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(
            @RequestParam(value = "id") Long id) {
        AppealVO one = appealService.findOneAppealVO(id);
        if (one == null)
            return error("Data is empty!You should check parameter (id)!");
        return success(one);
    }

    //查询断言
    private List<BooleanExpression> getBooleanExpressionList(AppealStatus status, OrderStatus orderStatus) {
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        QAppeal qAppeal = QAppeal.appeal;
        if (status != null)
            booleanExpressionList.add(qAppeal.status.eq(status));
        if (orderStatus != null) {
            booleanExpressionList.add(qAppeal.order.status.eq(orderStatus));
        }
        return booleanExpressionList;
    }

    /**
     * 申诉已处理  取消订单
     *
     * @param orderSn
     * @return
     * @throws InformationExpiredException
     */
    @RequiresPermissions("otc:appeal:page-query")
    @PostMapping("cancel-order")
    @ApiOperation(value = "申诉处理  取消订单")
    @AccessLog(module = AdminModule.OTC, operation = "申诉处理  取消订单")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrder(long appealId, String orderSn, @RequestParam(value = "banned", defaultValue = "false") boolean banned,
                                     String code, @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin) throws InformationExpiredException {
        // 验证码校验
        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.APPEAL_CANCEL_PHONE_PREFIX);
        // 查询申诉单
        Appeal appeal = appealService.findOne(appealId);
        Assert.notNull(appeal, "申诉单不存在");
        Long initiatorId = appeal.getInitiatorId();
        Long associateId = appeal.getAssociateId();
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ADMIN_ORDER_NOT_EXISTS"));
        int ret = getRet(order, initiatorId, associateId);
        isTrue(ret != 0, msService.getMessage("REQUEST_FAILED"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.APPEAL), msService.getMessage("ORDER_NOT_ALLOW_CANCEL"));
        //取消订单
        if (!(orderService.cancelOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        MessageResult result = success("");
        if (ret == 1) {
            //banned为true 禁用账户
            Member member1 = memberService.findOne(initiatorId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
            result = cancel(order, order.getNumber(), associateId);
        } else if (ret == 2) {
            Member member1 = memberService.findOne(initiatorId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
            result = cancel(order, add(order.getNumber(), order.getCommission()), associateId);
        } else if (ret == 3) {
            Member member1 = memberService.findOne(associateId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
            result = cancel(order, add(order.getNumber(), order.getCommission()), initiatorId);
        } else if (ret == 4) {
            Member member1 = memberService.findOne(associateId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
            //卖家没有付钱  取消订单 将币退给用户
            result = cancel(order, order.getNumber(), initiatorId);
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        appeal.setDealWithTime(DateUtil.getCurrentDate());
        appeal.setIsSuccess(BooleanEnum.IS_FALSE);
        appeal.setStatus(AppealStatus.PROCESSED);
        appealService.save(appeal);
        // 提醒申诉人，订单已被取消
        sendInfo(memberService.findOne(initiatorId), false, false, orderSn);
        // 提醒被申诉人，订单已被取消
        sendInfo(memberService.findOne(associateId), true, false, orderSn);

        return result;
    }

    private void sendInfo(Member member, boolean isAppealed, boolean isSuccess, String orderSn) {
        String[] action = new String[]{"申诉", "被申诉"};
        String[] resolve = new String[]{"成功放币", "被撤销"};
        String html = "<html>\n" +
                "<body>\n" +
                "<div>\n" +
                "    <br/>您${action}的订单已${resolve}，订单号${orderSn}。如果您对该订单有疑问，可联系客服处理。<br/>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
        html = html.replace("${orderSn}", orderSn);
        Map<String, String> map = new HashMap<>(4);
        map.put("ids", orderSn.substring(0, orderSn.length() / 2));
        map.put("ide", orderSn.substring(orderSn.length() / 2));
        if (isAppealed) {
            html = html.replace("${action}", action[1]);
            map.put("action", action[1]);
        } else {
            html = html.replace("${action}", action[0]);
            map.put("action", action[0]);
        }
        if (isSuccess) {
            html = html.replace("${resolve}", resolve[0]);
            map.put("resolve", resolve[0]);
        } else {
            html = html.replace("${resolve}", resolve[1]);
            map.put("resolve", resolve[1]);
        }
        int templateId = "86".equals(member.getCountry().getAreaCode()) ? 42350 : 42351;
        remindService.sendAdminInfo(member, html, templateId, map);
    }


    private MessageResult cancel(Order order, BigDecimal amount, Long memberId) throws InformationExpiredException {
        OtcWallet memberWallet;
        //更改广告
        if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), amount)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        memberWallet = otcWalletService.findByOtcCoinAndMemberId(memberId, order.getCoin());
        MessageResult result = otcWalletService.thawBalance(memberWallet, amount);
        if (result.getCode() == 0) {
            return MessageResult.success(msService.getMessage("SUCCESS"));
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    private int getRet(Order order, Long initiatorId, Long associateId) {
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(initiatorId)) {
            //代表该申诉者是广告发布者，并且该订单属于该商家创建的订单 卖家associateId 代表该商家付了钱 卖方用户没有放币
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(initiatorId)) {
            //代表该申诉者不是广告发布者，但是是该订单的付款者  卖家associateId 代表用户付了卖方钱商家没有放币
            ret = 2;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(associateId)) {
            //代表该申诉者是广告发布者，但是是付款者   卖家initiatorId（代表商家拨了币没有收到钱）
            ret = 3;
        } else if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(associateId)) {
            //代表该申诉者不是广告发布者，但不是付款者  卖家initiatorId（代表用户拨了币(进了冻结状态)没有收到钱）
            ret = 4;
        }
        return ret;
    }


    /**
     * 申诉处理 订单放行（放币）
     *
     * @param orderSn
     * @return
     */
    @RequiresPermissions("otc:appeal:page-query")
    @PostMapping("release-coin")
    @ApiOperation(value = "申诉处理  订单放行（放币）")
    @AccessLog(module = AdminModule.OTC, operation = "申诉处理  订单放行（放币）")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult confirmRelease(long appealId, String orderSn, @RequestParam(value = "banned", defaultValue = "false") boolean banned,
                                        String code, @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin) throws Exception {

        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.APPEAL_CONFIRM_PHONE_PREFIX);

        Appeal appeal = appealService.findOne(appealId);
        Assert.notNull(appeal, "申诉单不存在");
        Long initiatorId = appeal.getInitiatorId();
        Long associateId = appeal.getAssociateId();
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ADMIN_ORDER_NOT_EXISTS"));
        int ret = getRet(order, initiatorId, associateId);
        isTrue(ret != 0, msService.getMessage("REQUEST_FAILED"));
        isTrue(order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.APPEAL), msService.getMessage("ORDER_STATUS_EXPIRED"));
        if (ret == 1 || ret == 4) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else if ((ret == 2 || ret == 3)) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        //放行订单
        if (!(orderService.releaseOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        //后台处理申诉结果为放行---更改买卖双方钱包
        otcWalletService.transferAdmin(order, ret);

        if (ret == 1) {

            generateMemberTransaction(order, TransactionType.OTC_SELL, associateId, order.getCommission());

            generateMemberTransaction(order, TransactionType.OTC_BUY, initiatorId, BigDecimal.ZERO);

        } else if (ret == 2) {

            generateMemberTransaction(order, TransactionType.OTC_SELL, associateId, order.getCommission());

            generateMemberTransaction(order, TransactionType.OTC_BUY, initiatorId, BigDecimal.ZERO);

        } else if (ret == 3) {

            generateMemberTransaction(order, TransactionType.OTC_BUY, associateId, BigDecimal.ZERO);

            generateMemberTransaction(order, TransactionType.OTC_SELL, initiatorId, order.getCommission());
        } else {

            generateMemberTransaction(order, TransactionType.OTC_BUY, associateId, BigDecimal.ZERO);

            generateMemberTransaction(order, TransactionType.OTC_SELL, initiatorId, order.getCommission());
        }
        orderEvent.onOrderCompleted(order);

        //banned为true 禁用账户
        if (ret == 1 || ret == 2) {
            Member member1 = memberService.findOne(associateId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }

        } else {
            Member member1 = memberService.findOne(initiatorId);
            if (member1.getStatus() == CommonStatus.NORMAL && banned) {
                member1.setStatus(CommonStatus.ILLEGAL);
                memberService.save(member1);
            }
        }
        appeal.setDealWithTime(DateUtil.getCurrentDate());
        appeal.setIsSuccess(BooleanEnum.IS_TRUE);
        appeal.setStatus(AppealStatus.PROCESSED);
        appealService.save(appeal);
        // 提醒申诉人，订单已放行
        sendInfo(memberService.findOne(initiatorId), false, true, orderSn);
        // 提醒被申诉人，订单已放行
        sendInfo(memberService.findOne(associateId), true, true, orderSn);

        return MessageResult.success(msService.getMessage("SUCCESS"));
    }


    private void generateMemberTransaction(Order order, TransactionType type, long memberId, BigDecimal fee) {

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setSymbol(order.getCoin().getUnit());
        memberTransaction.setType(type);
        memberTransaction.setFee(fee);
        memberTransaction.setMemberId(memberId);
        if (type.equals(TransactionType.OTC_SELL)) {
            memberTransaction.setAmount(BigDecimal.ZERO.subtract(order.getNumber()));
        } else {
            memberTransaction.setAmount(order.getNumber());
        }
        memberTransactionService.save(memberTransaction);

    }
}
