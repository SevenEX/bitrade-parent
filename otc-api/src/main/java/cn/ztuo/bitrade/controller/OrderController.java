package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.coin.CoinExchangeFactory;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.dto.OrderScreen;
import cn.ztuo.bitrade.dto.OtcOrderOverview;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.chat.ChatMessageRecord;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.event.OrderEvent;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.remind.RemindService;
import cn.ztuo.bitrade.remind.RemindType;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.util.*;
import cn.ztuo.bitrade.vendor.provider.SMSProvider;
import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static cn.ztuo.bitrade.constant.BooleanEnum.IS_FALSE;
import static cn.ztuo.bitrade.constant.BooleanEnum.IS_TRUE;
import static cn.ztuo.bitrade.constant.PayMode.*;
import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;
import static cn.ztuo.bitrade.util.BigDecimalUtils.*;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * @author Seven
 * @date 2019年12月11日
 */
@RestController
@RequestMapping(value = "/order", method = RequestMethod.POST)
@Slf4j
@Api(tags = "otc订单管理")
public class OrderController extends BaseController {

    /* private static Logger logger = LoggerFactory.getLogger(OrderController.class);*/

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdvertiseService advertiseService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CoinExchangeFactory coins;

    @Autowired
    private OrderEvent orderEvent;

    @Autowired
    private AppealService appealService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private OrderDetailAggregationService orderDetailAggregationService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private DataDictionaryService dictionaryService;

    @Autowired
    private OtcWalletService otcWalletService;

    @Value("${bdtop.system.order.sms:0}")
    private int notice;

    @Value("${sms.driver}")
    private String driverName;

    @Autowired
    private SMSProvider smsProvider;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MemberGradeService gradeService;

    @Autowired
    private IntegrationRecordService integrationRecordService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private RemindService remindService;

    /**
     * 买入，卖出详细信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "pre", method = RequestMethod.POST)
    @ApiOperation(value = "查询买入/卖出详细信息")
    @MultiDataSource(name = "second")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "广告id", required = true, dataType = "Long"),
    })
    public MessageResult preOrderInfo(long id) {
        Advertise advertise = advertiseService.findOne(id);
        notNull(advertise, msService.getMessage("PARAMETER_ERROR"));
        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES), msService.getMessage("PARAMETER_ERROR"));
        Member member = advertise.getMember();
        OtcCoin otcCoin = advertise.getCoin();
        PreOrderInfo preOrderInfo = PreOrderInfo.builder()
                .advertiseType(advertise.getAdvertiseType())
                .country(advertise.getCountry().getZhName())
                .emailVerified(member.getEmail() == null ? IS_FALSE : IS_TRUE)
                .idCardVerified(member.getIdNumber() == null ? IS_FALSE : IS_TRUE)
                .maxLimit(advertise.getMaxLimit())
                .minLimit(advertise.getMinLimit())
                .number(advertise.getRemainAmount())
                .otcCoinId(otcCoin.getId())
                .payMode(advertise.getPayMode())
                .phoneVerified(member.getMobilePhone() == null ? IS_FALSE : IS_TRUE)
                .timeLimit(advertise.getTimeLimit())
                .transactions(member.getTransactions())
                .unit(otcCoin.getUnit())
                .username(member.getUsername())
                .remark(advertise.getRemark())
                .level(member.getMemberLevel().getOrdinal())
                .build();
        //处理可交易的最大数量
        if (advertise.getAdvertiseType().equals(AdvertiseType.SELL)) {
            MemberGrade grade = gradeService.findOne(member.getMemberGradeId());
            //计算手续费
            BigDecimal maxTransactions = divDown(advertise.getRemainAmount(), add(BigDecimal.ONE, grade.getOtcFeeRate()));
            preOrderInfo.setMaxTradableAmount(maxTransactions);
        } else {
            preOrderInfo.setMaxTradableAmount(advertise.getRemainAmount());
        }
        if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            preOrderInfo.setPrice(advertise.getPrice());
        } else {
            BigDecimal marketPrice = coins.getLegalCurrencyRate(advertise.getCountry().getLocalCurrency(), otcCoin.getUnit());
            preOrderInfo.setPrice(mulRound(marketPrice, rate(advertise.getPremiseRate()), 2));
        }
        MessageResult result = MessageResult.success();
        result.setData(preOrderInfo);
        return result;
    }

    /**
     * 买币
     *
     * @param id
     * @param coinId
     * @param price
     * @param money
     * @param amount
     * @param remark
     * @param user
     * @return
     * @throws InformationExpiredException
     */
    @RequestMapping(value = "buy", method = RequestMethod.POST)
    @ApiOperation(value = "买币")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "广告id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "coinId", value = "币种id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "price", value = "单价", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "money", value = "金额", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "amount", value = "数量", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, dataType = "String"),
            @ApiImplicitParam(name = "mode", value = "模式", required = false, dataType = "Integer"),
            @ApiImplicitParam(name = "payMode", value = "买家选择的付款方式", required = false, dataType = "String"),
    })
    public MessageResult buy(long id, long coinId, BigDecimal price, BigDecimal money,
                             BigDecimal amount, String remark, String payMode,
                             @RequestParam(value = "mode", defaultValue = "0") Integer mode,
                             @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws InformationExpiredException {
        int cancelNum = orderService.countByCustomerIdAndStatusAndAdvertiseTypeAndCancelTimeBetween(user.getId(),OrderStatus.CANCELLED,AdvertiseType.SELL, DateUtils.addDays(DateUtil.getCurrentDate(),-1),DateUtil.getCurrentDate());
        isTrue(cancelNum<3, msService.getMessage("NOT_ALLOW_BUY"));
        Advertise advertise = advertiseService.findOne(id);
        if (advertise == null || !advertise.getAdvertiseType().equals(AdvertiseType.SELL)) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        isTrue(user.getId()!=advertise.getMember().getId(), msService.getMessage("NOT_ALLOW_BUY_BY_SELF"));
        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES), msService.getMessage("ALREADY_PUT_OFF"));
        OtcCoin otcCoin = advertise.getCoin();
        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        Member member = memberService.findOne(user.getId());
        if (member.getMemberLevel().getOrdinal() < 1) {
            return MessageResult.error(msService.getMessage("NO_REAL_NAME"));
        }
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error(msService.getMessage("CANNOT_TRADE"));
        }
        OtcWallet memberWallet = otcWalletService.findByOtcCoinAndMemberId(user.getId(), otcCoin);
        if (memberWallet == null) {
            //如果法币账户不存在新建
            OtcWallet otcWalletNew = new OtcWallet();
            Coin coin = coinService.findByUnit(otcCoin.getUnit());
            otcWalletNew.setCoin(coin);
            otcWalletNew.setIsLock(0);
            otcWalletNew.setMemberId(member.getId());
            otcWalletNew.setBalance(BigDecimal.ZERO);
            otcWalletNew.setFrozenBalance(BigDecimal.ZERO);
            otcWalletNew.setReleaseBalance(BigDecimal.ZERO);
            otcWalletNew.setVersion(0);
            memberWallet = otcWalletService.save(otcWalletNew);
            if (memberWallet == null) {
                return MessageResult.error(msService.getMessage("CREATE_WALLET_ERROR"));
            }
        }
        if (memberWallet.getIsLock() == 1) {
            return MessageResult.error(msService.getMessage("WALLET_IS_LOCK"));
        }
        if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            isTrue(isEqual(price, advertise.getPrice()), msService.getMessage("PRICE_EXPIRED"));
        } else {
            BigDecimal marketPrice = coins.getLegalCurrencyRate(advertise.getCountry().getLocalCurrency(), otcCoin.getUnit());
            isTrue(isEqual(price, mulRound(rate(advertise.getPremiseRate()), marketPrice, 2)), msService.getMessage("PRICE_EXPIRED"));
        }
        if (mode == 0) {
            money =  money.setScale(2,RoundingMode.HALF_UP);
            amount = money.divide(advertise.getPrice(), otcCoin.getCoinScale(), RoundingMode.DOWN);
        } else {
            amount = amount.setScale(otcCoin.getCoinScale(),RoundingMode.DOWN);
            money = amount.multiply(advertise.getPrice()).setScale(2,RoundingMode.HALF_UP);
        }
        isTrue(compare(money, advertise.getMinLimit()), msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString() + " CNY");
        isTrue(compare(advertise.getMaxLimit(), money), msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString() + " CNY");
        String[] pay = advertise.getPayMode().split(",");
        //获取手续费比例
        Member sellMember = memberService.findOne(advertise.getMember().getId());
        MemberGrade grade = gradeService.findOne(sellMember.getMemberGradeId());
        //计算手续费
        BigDecimal commission = mulRound(amount, grade.getOtcFeeRate());

        isTrue(compare(advertise.getRemainAmount(), add(commission, amount)), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(commission);
        order.setCountry(advertise.getCountry().getZhName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(member.getRealName());
        order.setMemberId(advertise.getMember().getId());
        order.setMemberName(advertise.getMember().getUsername());
        order.setMemberRealName(advertise.getMember().getRealName());
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(amount);
        order.setPayMode(StringUtils.isEmpty(payMode) ? advertise.getPayMode() : payMode);
        order.setPrice(price);
        order.setRemark(remark);
        order.setTimeLimit(advertise.getTimeLimit());
        //随机生成6位付款参考号
        order.setReferenceNumber(GeneratorUtil.getNonceNumberString(6));
        Arrays.stream(pay).forEach(x -> {
            if (String.valueOf(ALIPAY.getOrdinal()).equals(x)) {
                order.setAlipay(advertise.getMember().getAlipay());
            } else if (String.valueOf(WECHAT.getOrdinal()).equals(x)) {
                order.setWechatPay(advertise.getMember().getWechatPay());
            } else if (String.valueOf(BANK.getOrdinal()).equals(x)) {
                order.setBankInfo(advertise.getMember().getBankInfo());
            }
        });
        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), add(commission, amount))) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        Order order1 = orderService.saveOrder(order);
        if (order1 != null) {
            sendMessageAndSaveContent(user, advertise, order1);
            MessageResult result = MessageResult.success(msService.getMessage("CREATE_ORDER_SUCCESS"));
            result.setData(order1.getOrderSn());
            return result;
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    /**
     * 发送短信并保存短信内容
     *
     * @param user
     * @param advertise
     * @param order1
     */
    private void sendMessageAndSaveContent(AuthMember user, Advertise advertise, Order order1) {
        if (notice == 1) {
            try {
                sendSms(advertise, user.getName());
            } catch (Exception e) {
                log.info("发送失败");
                e.printStackTrace();
            }
        }
        /**
         * 下单后，将自动回复记录添加到mongodb
         */
        if (advertise.getAuto() == BooleanEnum.IS_TRUE) {
            ChatMessageRecord chatMessageRecord = new ChatMessageRecord();
            chatMessageRecord.setOrderId(order1.getOrderSn());
            chatMessageRecord.setUidFrom(order1.getMemberId().toString());
            chatMessageRecord.setUidTo(order1.getCustomerId().toString());
            chatMessageRecord.setNameFrom(order1.getMemberName());
            chatMessageRecord.setNameTo(order1.getCustomerName());
            chatMessageRecord.setContent(advertise.getAutoword());
            chatMessageRecord.setSendTime(Calendar.getInstance().getTimeInMillis());
            chatMessageRecord.setSendTimeStr(DateUtil.getDateTime());
            //自动回复消息保存到mogondb
            mongoTemplate.insert(chatMessageRecord, "chat_message");
        }
    }

    /**
     * 卖币
     *
     * @param id
     * @param coinId
     * @param price
     * @param money
     * @param amount
     * @param remark
     * @param user
     * @return
     * @throws InformationExpiredException
     */
    @RequestMapping(value = "sell")
    @ApiOperation(value = "卖币")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "广告id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "coinId", value = "币种id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "price", value = "单价", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "money", value = "金额", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "amount", value = "数量", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "remark", value = "备注", required = false, dataType = "String"),
            @ApiImplicitParam(name = "mode", value = "模式", required = false, dataType = "Integer"),
    })
    public MessageResult sell(long id, long coinId, BigDecimal price, BigDecimal money,
                              BigDecimal amount, String remark,
                              @RequestParam(value = "mode", defaultValue = "0") Integer mode,
                              @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws InformationExpiredException {
        log.info("登陆用户的信息={}", user);
        Advertise advertise = advertiseService.findOne(id);
        if (advertise == null || !advertise.getAdvertiseType().equals(AdvertiseType.BUY)) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        isTrue(user.getId() != advertise.getMember().getId(), msService.getMessage("NOT_ALLOW_SELL_BY_SELF"));
        isTrue(advertise.getStatus().equals(AdvertiseControlStatus.PUT_ON_SHELVES), msService.getMessage("ALREADY_PUT_OFF"));
        OtcCoin otcCoin = advertise.getCoin();
        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        if (advertise.getPriceType().equals(PriceType.REGULAR)) {
            isTrue(isEqual(price, advertise.getPrice()), msService.getMessage("PRICE_EXPIRED"));
        } else {
            BigDecimal marketPrice = coins.getLegalCurrencyRate(advertise.getCountry().getLocalCurrency(), otcCoin.getUnit());
            isTrue(isEqual(price, mulRound(rate(advertise.getPremiseRate()), marketPrice, 2)), msService.getMessage("PRICE_EXPIRED"));
        }
        if (mode == 0) {
            money =  money.setScale(2,RoundingMode.HALF_UP);
            amount = money.divide(advertise.getPrice(), otcCoin.getCoinScale(), RoundingMode.DOWN);
        } else {
            amount = amount.setScale(otcCoin.getCoinScale(),RoundingMode.DOWN);
            money = amount.multiply(advertise.getPrice()).setScale(2,RoundingMode.HALF_UP);
        }
        isTrue(compare(money, advertise.getMinLimit()), msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString() + " CNY");
        isTrue(compare(advertise.getMaxLimit(), money), msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString() + " CNY");
        isTrue(compare(advertise.getRemainAmount(), amount), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        Member member = memberService.findOne(user.getId());
        if (member.getMemberLevel().getOrdinal() < 1) {
            return MessageResult.error(msService.getMessage("NO_REAL_NAME"));
        }
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error(msService.getMessage("CANNOT_TRADE"));
        }
        //检查用户绑定的支付方式与广告是否匹配
        boolean flag = true;
        if (advertise.getPayMode() == null || advertise.getPayMode().equals("")) {
            return MessageResult.error(msService.getMessage("ERROR_ADVERTISE_PAY_MODE"));
        }
        String[] payModes = advertise.getPayMode().split(",");
        for (String payMode : payModes) {
            if (ALIPAY.getOrdinal() == Integer.valueOf(payMode) && member.getAlipay() != null) {
                flag = false;
            } else if (WECHAT.getOrdinal() == Integer.valueOf(payMode) && member.getWechatPay() != null) {
                flag = false;
            } else if (BANK.getOrdinal() == Integer.valueOf(payMode) && member.getBankInfo() != null) {
                flag = false;
            }
        }
        if (flag) {
            return MessageResult.error(msService.getMessage("PAY_MODE_MISMATCH"));
        }
        OtcWallet wallet = otcWalletService.findByOtcCoinAndMemberId(user.getId(), otcCoin);
        if (wallet == null) {
            return MessageResult.error(msService.getMessage("NOT_OTC_WALLET"));

        }
        if (wallet.getIsLock() == 1) {
            return MessageResult.error(msService.getMessage("WALLET_IS_LOCK"));
        }
        isTrue(wallet.getIsLock() == 0, msService.getMessage("WALLET_IS_LOCK"));
        isTrue(compare(wallet.getBalance(), amount), msService.getMessage("INSUFFICIENT_BALANCE"));
        //获取手续费比例
        MemberGrade grade = gradeService.findOne(advertise.getMember().getMemberGradeId());
        log.info("商家的等级grade={}", grade);
        //计算手续费
        BigDecimal commission = mulRound(amount, grade.getOtcFeeRate());
        log.info("该手续费commission={}", commission);
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(commission);
        order.setCountry(advertise.getCountry().getZhName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(member.getRealName());
        order.setMemberId(advertise.getMember().getId());
        order.setMemberName(advertise.getMember().getUsername());
        order.setMemberRealName(advertise.getMember().getRealName());
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setPrice(price);
        order.setRemark(remark);
        //随机生成6位付款参考号
        order.setReferenceNumber(GeneratorUtil.getNonceNumberString(6));
        order.setTimeLimit(advertise.getTimeLimit());
        String[] pay = advertise.getPayMode().split(",");
        MessageResult result = MessageResult.error(msService.getMessage("CREATE_ORDER_SUCCESS"));
        Arrays.stream(pay).forEach(x -> {
            if (ALIPAY.getOrdinal() == Integer.valueOf(x)) {
                if (member.getAlipay() != null) {
                    result.setCode(0);
                    order.setAlipay(member.getAlipay());
                }
            } else if (WECHAT.getOrdinal() == Integer.valueOf(x)) {
                if (member.getWechatPay() != null) {
                    result.setCode(0);
                    order.setWechatPay(member.getWechatPay());
                }
            } else if (BANK.getOrdinal() == Integer.valueOf(x)) {
                if (member.getBankInfo() != null) {
                    result.setCode(0);
                    order.setBankInfo(member.getBankInfo());
                }
            }
        });
        isTrue(result.getCode() == 0, msService.getMessage("AT_LEAST_SUPPORT_PAY"));
        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), amount)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        //冻结用户余额=卖出数量
        if (!(otcWalletService.freezeBalance(wallet, amount).getCode() == 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        Order order1 = orderService.saveOrder(order);
        if (order1 != null) {
            remindService.sendInfo(advertise.getMember(), order, RemindType.ORDER);
            //sendMessageAndSaveContent(user, advertise, order1);
            result.setData(order1.getOrderSn());
            return result;
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }


    /**
     * 我的订单
     *
     * @param user
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "self")
    @ApiOperation(value = "分页查询我的订单")
    @MultiDataSource(name = "second")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer", defaultValue = "10"),
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String")
    })
    public MessageResult myOrder(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize, OrderScreen orderScreen) {
        Page<Order> page = orderService.pageQuery(pageNo, pageSize, user.getId(), orderScreen);
        List<Long> memberIdList = new ArrayList<>();
        page.forEach(order -> {
            if (!memberIdList.contains(order.getMemberId())) {
                memberIdList.add(order.getMemberId());
            }
            if (!memberIdList.contains(order.getCustomerId())) {
                memberIdList.add(order.getCustomerId());
            }
        });
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        booleanExpressionList.add(QMember.member.id.in(memberIdList));
        PageResult<Member> memberPage = memberService.queryWhereOrPage(booleanExpressionList, null, null);
        Page<ScanOrder> scanOrders = page.map(x -> ScanOrder.toScanOrder(x, user.getId()));
        for (ScanOrder scanOrder : scanOrders) {
            for (Member member : memberPage.getContent()) {
                if (scanOrder.getMemberId().equals(member.getId())) {
                    scanOrder.setAvatar(member.getAvatar());
                }
            }
        }
        MessageResult result = MessageResult.success();
        result.setData(scanOrders);
        return result;
    }

    /**
     * 订单详情
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "detail")
    @ApiOperation(value = "订单详情")
    @MultiDataSource(name = "second")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String")
    })
    public MessageResult queryOrder(String orderSn, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        MessageResult result = MessageResult.success();
        //用户信息
        Member customer = memberService.findOne(order.getCustomerId());
        //广告商信息
        Member member = memberService.findOne(order.getMemberId());
        //广告信息
        Advertise ad = advertiseService.findOne(order.getAdvertiseId());
        Appeal appeal = appealService.findByOrder(order);
        OrderDetail info = OrderDetail.builder().orderSn(orderSn)
                .referenceNumber(order.getReferenceNumber())
                .unit(order.getCoin().getUnit())
                .status(order.getStatus())
                .amount(order.getNumber())
                .price(order.getPrice())
                .money(order.getMoney())
                .payTime(order.getPayTime())
                .createTime(order.getCreateTime().getTime())
                .timeLimit(order.getTimeLimit())
                .myId(user.getId())
                .remark(ad.getRemark())
                .appeal(appeal)
                .payMode(order.getPayMode())
                .serverTime(Calendar.getInstance().getTimeInMillis())
                .build();
        String[] pay = order.getPayMode().split(",");
        if(!order.getMemberId().equals(user.getId()) && !order.getCustomerId().equals(user.getId())) {
            return MessageResult.error(msService.getMessage("ORDER_NOT_EXISTS"));
        }
        if (order.getAdvertiseType() == AdvertiseType.BUY) {
            PayInfo payInfo = PayInfo.builder()
                    .realName(customer.getRealName())
                    .build();
            Arrays.stream(pay).forEach(x -> {
                if (ALIPAY.getOrdinal() == Integer.valueOf(x)) {
                    payInfo.setAlipay(customer.getAlipay());
                } else if (WECHAT.getOrdinal() == Integer.valueOf(x)) {
                    payInfo.setWechatPay(customer.getWechatPay());
                } else if (BANK.getOrdinal() == Integer.valueOf(x)) {
                    payInfo.setBankInfo(customer.getBankInfo());
                }
            });
            info.setPayInfo(payInfo);
            if (order.getMemberId().equals(user.getId())) {
                info.setType(AdvertiseType.BUY);
               /* if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getCustomerRealName());
                }*/
            } else {
                info.setType(AdvertiseType.SELL);
                /*if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getMemberRealName());
                }*/
            }
        } else {
            PayInfo payInfo = PayInfo.builder()
                    .realName(member.getRealName())
                    .build();
            Arrays.stream(pay).forEach(x -> {
                if (ALIPAY.getOrdinal() == Integer.valueOf(x)) {
                    payInfo.setAlipay(member.getAlipay());
                } else if (WECHAT.getOrdinal() == Integer.valueOf(x)) {
                    payInfo.setWechatPay(member.getWechatPay());
                } else if (BANK.getOrdinal() == Integer.valueOf(x)) {
                    payInfo.setBankInfo(member.getBankInfo());
                }
            });
            info.setPayInfo(payInfo);
            if (order.getCustomerId().equals(user.getId())) {
                info.setType(AdvertiseType.BUY);
                /*if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getCustomerRealName());
                }*/
            } else {
                info.setType(AdvertiseType.SELL);
               /* if (info.getPayInfo() != null) {
                    info.getPayInfo().setRealName(order.getMemberRealName());
                }*/
            }
        }
        if (order.getMemberId().equals(user.getId())) {
            info.setHisId(order.getCustomerId());
            info.setOtherSide(order.getCustomerName());
            info.setCommission(order.getCommission());
        }
        else if (order.getCustomerId().equals(user.getId())) {
            info.setHisId(order.getMemberId());
            info.setOtherSide(order.getMemberName());
            info.setCommission(BigDecimal.ZERO);
            if (order.getAdvertiseType().equals(AdvertiseType.SELL) && info.getCreateTime() + info.getTimeLimit() * 60 * 1000 <= info.getServerTime() && info.getStatus().equals(OrderStatus.NONPAYMENT)) {
                info.setStatus(OrderStatus.CANCELLED);
            }
        }
        result.setData(info);
        return result;
    }

    /**
     * 取消订单
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "cancel")
    @ApiOperation(value = "取消订单")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String")
    })
    public MessageResult cancelOrder(String orderSn, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws InformationExpiredException {
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            //代表该广告是买币广告 且该订单属于该付款人的订单（即该客户创建的买币下的订单）
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(user.getId())) {
            //代表该广告是卖广告 属于商家的卖单 但是该用户付款客户 二者不同
            ret = 2;
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID), msService.getMessage("ORDER_NOT_ALLOW_CANCEL"));
        OtcWallet memberWallet;
        //取消订单
        if (!(orderService.cancelOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        if (ret == 1) {
            //更改广告
            //创建订单的时候减少了remainAmount，增加了dealAmount，撤销时减少dealAmount的金额，不增加remainAmount的金额(订单剩余金额始终下降)，返还到账户余额
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
            memberWallet = otcWalletService.findByOtcCoinAndMemberId(order.getCustomerId(), order.getCoin());
            MessageResult result = otcWalletService.thawBalance(memberWallet, order.getNumber());
            if (result.getCode() == 0) {
                return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"));
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
            memberWallet = otcWalletService.findByOtcCoinAndMemberId(order.getMemberId(), order.getCoin());
            MessageResult result = otcWalletService.thawBalance(memberWallet, add(order.getNumber(), order.getCommission()));
            if (result.getCode() == 0) {
                return MessageResult.success(msService.getMessage("CANCEL_SUCCESS"));
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }
    }

    /**
     * 确认付款
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "pay")
    @ApiOperation(value = "确认付款")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "payMode", value = "转账方式", required = true, dataType = "String")
    })
    public MessageResult payOrder(String orderSn, String payMode, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws InformationExpiredException {
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = 0;
        long sellerId = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(user.getId())) {
            //代表该广告是买币广告 且该订单属于该付款人的订单（即该客户创建的买币下的订单）
            ret = 1;
            sellerId = order.getCustomerId();
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(user.getId())) {
            //代表该广告是卖广告 属于商家的卖单 但是该用户付款客户 二者不同
            ret = 2;
            sellerId = order.getMemberId();
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT), msService.getMessage("ORDER_STATUS_EXPIRED"));
        isTrue(compare(new BigDecimal(order.getTimeLimit()), DateUtil.diffMinute(order.getCreateTime())), msService.getMessage("ORDER_ALREADY_AUTO_CANCEL"));
        int is = orderService.payForOrder(orderSn, payMode);
        if (is > 0) {
            /**
             * 聚合otc订单手续费等明细存入mongodb
             */
            OrderDetailAggregation aggregation = new OrderDetailAggregation();
            BeanUtils.copyProperties(order, aggregation);
            aggregation.setUnit(order.getCoin().getUnit());
            aggregation.setOrderId(order.getOrderSn());
            aggregation.setFee(order.getCommission().doubleValue());
            aggregation.setAmount(order.getNumber().doubleValue());
            aggregation.setType(OrderTypeEnum.OTC);
            aggregation.setTime(Calendar.getInstance().getTimeInMillis());
            orderDetailAggregationService.save(aggregation);
            // 查询并知商家，买家已付款
            remindService.sendInfo(memberService.findOne(sellerId), order, RemindType.PAY);
            MessageResult result = MessageResult.success(msService.getMessage("PAY_SUCCESS"));
            result.setData(order);
            return result;
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }

    }

    /**
     * 订单放行
     *
     * @param orderSn
     * @param user
     * @return
     */
    @RequestMapping(value = "release")
    @ApiOperation(value = "订单放行")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderSn", value = "订单编号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "jyPassword", value = "交易密码", required = true, dataType = "String")
    })
    public MessageResult confirmRelease(String orderSn, String jyPassword, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws Exception {
        Assert.hasText(jyPassword, msService.getMessage("MISSING_JYPASSWORD"));
        Member member = memberService.findOne(user.getId());
        String mbPassword = member.getJyPassword();
        Assert.hasText(mbPassword, msService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(Md5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(mbPassword), msService.getMessage("ERROR_JYPASSWORD"));
        Order order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = 0;
        Long customerId = null;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getCustomerId().equals(user.getId())) {
            //代表是商家买币 客户卖币 广告的发布者是商家 放行者是客户 两者不同
            ret = 1;
            customerId = order.getMemberId();
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getMemberId().equals(user.getId())) {
            //代表 广告是卖单 某个用户买 这个时候订单的memberId是广告人的id 就是广告的发布者and放行者
            ret = 2;
            customerId = order.getCustomerId();
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.PAID), msService.getMessage("ORDER_STATUS_EXPIRED"));
        if (ret == 1) {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else {
            //更改广告
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }
        //放行订单
        if (!(orderService.releaseOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        //更改钱包
        otcWalletService.transfer(order, ret);
        DataDictionary dictionary = dictionaryService.findByBond(SysConstant.INTEGRATION_GIVING_OTC_BUY_CNY_RATE);
        if (ret == 1) {
            //客户卖币 不收取手续费
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setSymbol(order.getCoin().getUnit());
            memberTransaction.setType(TransactionType.OTC_SELL);
            memberTransaction.setFee(BigDecimal.ZERO);
//            memberTransaction.setFee(order.getCommission());
            memberTransaction.setMemberId(user.getId());
            memberTransaction.setAmount(BigDecimal.ZERO.subtract(order.getNumber()));
            memberTransactionService.save(memberTransaction);
            //商家买币收手续费
            MemberTransaction memberTransaction1 = new MemberTransaction();
            memberTransaction1.setAmount(sub(order.getNumber(), order.getCommission()));
            memberTransaction1.setType(TransactionType.OTC_BUY);
            memberTransaction1.setMemberId(order.getMemberId());
            memberTransaction1.setSymbol(order.getCoin().getUnit());
            memberTransaction1.setFee(order.getCommission());
//            memberTransaction1.setFee(BigDecimal.ZERO);
            memberTransactionService.save(memberTransaction1);

            /**
             * 放行成功 广告买单直接给广告拥有者加积分记录(商家) 广告卖单给用户加积分记录
             */
            try {
                Long merchantsMemberId = order.getMemberId();
                //法币钱转积分*比例
                BigDecimal money = order.getMoney().multiply(new BigDecimal(dictionary.getValue()));
                Member merchantsMember = memberService.findOne(merchantsMemberId);
                Long integration = money.setScale(0).longValue();
                Long totalIntegration = integration + merchantsMember.getIntegration();
                merchantsMember.setIntegration(totalIntegration);
                //判断等级是否满足 V5 V6 不改变等级 只加积分
                MemberGrade grade = gradeService.findOne(merchantsMember.getMemberGradeId());
                if (grade.getId() != 5L && grade.getId() != 6L) {
                    if (grade.getGradeBound() < totalIntegration) {
                        merchantsMember.setMemberGradeId(merchantsMember.getMemberGradeId() + 1);
                    }
                }
                memberService.save(merchantsMember);
                IntegrationRecord integrationRecord = new IntegrationRecord();
                integrationRecord.setAmount(integration);
                integrationRecord.setMemberId(merchantsMember.getId());
                integrationRecord.setCreateTime(new Date());
                integrationRecord.setType(IntegrationRecordType.LEGAL_RECHARGE_GIVING);
                integrationRecordService.save(integrationRecord);
            } catch (Exception e) {
                log.info("法币充值积分赠送失败,={}", e);
            }

        } else {
            //商家卖币
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setSymbol(order.getCoin().getUnit());
            memberTransaction.setType(TransactionType.OTC_SELL);
            memberTransaction.setFee(order.getCommission());
            memberTransaction.setMemberId(user.getId());
            memberTransaction.setAmount(BigDecimal.ZERO.subtract(order.getNumber()));
            memberTransactionService.save(memberTransaction);
            //客户买币
            MemberTransaction memberTransaction1 = new MemberTransaction();
            memberTransaction1.setAmount(order.getNumber());
            memberTransaction1.setType(TransactionType.OTC_BUY);
            memberTransaction1.setMemberId(order.getCustomerId());
            memberTransaction1.setSymbol(order.getCoin().getUnit());
            memberTransaction1.setFee(BigDecimal.ZERO);
            memberTransactionService.save(memberTransaction1);
            /**
             * 买家得到积分
             */
            try {
                Long customerMemberId = order.getCustomerId();
                //法币钱转积分*比例
                BigDecimal money = order.getMoney().multiply(new BigDecimal(dictionary.getValue()));
                Member customersMember = memberService.findOne(customerMemberId);
                Long integration = money.setScale(0).longValue();
                Long totalIntegration = integration + customersMember.getIntegration();
                customersMember.setIntegration(totalIntegration);
                //判断等级是否满足 V5 V6 不改变等级 只加积分
                MemberGrade grade = gradeService.findOne(customersMember.getMemberGradeId());
                if (grade.getId() != 5L && grade.getId() != 6L) {
                    if (grade.getGradeBound() < totalIntegration) {
                        customersMember.setMemberGradeId(customersMember.getMemberGradeId() + 1);
                    }
                }
                memberService.save(customersMember);
                IntegrationRecord integrationRecord = new IntegrationRecord();
                integrationRecord.setAmount(integration);
                integrationRecord.setMemberId(customersMember.getId());
                integrationRecord.setCreateTime(new Date());
                integrationRecord.setType(IntegrationRecordType.LEGAL_RECHARGE_GIVING);
                integrationRecordService.save(integrationRecord);
            } catch (Exception e) {
                log.info("法币充值积分则送失败={}", e);
            }

        }
        //法币交易佣金处理
        orderEvent.onOrderCompleted(order);
        // 发送提醒
        remindService.sendInfo(memberService.findOne(customerId), order, RemindType.RELEASE);
        return MessageResult.success(msService.getMessage("RELEASE_SUCCESS"));
    }

    /**
     * 申诉
     *
     * @param appealApply
     * @param bindingResult
     * @param user
     * @return
     * @throws InformationExpiredException
     */
    @RequestMapping(value = "appeal")
    @ApiOperation(value = "申诉")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult appeal(@Valid AppealApply appealApply, BindingResult bindingResult,
                                @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws InformationExpiredException {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Order order = orderService.findOneByOrderSn(appealApply.getOrderSn());
        int ret = 0;
        Long infoMemberId = null;
        if (order.getMemberId().equals(user.getId())) {
            ret = 1;
            infoMemberId = order.getCustomerId();
        } else if (order.getCustomerId().equals(user.getId())) {
            ret = 2;
            infoMemberId = order.getMemberId();
        }
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.PAID), msService.getMessage("NO_APPEAL"));
        if (!(orderService.updateOrderAppeal(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        Appeal appeal = new Appeal();
        appeal.setInitiatorId(user.getId());
        if (ret == 1) {
            appeal.setAssociateId(order.getCustomerId());
        } else {
            appeal.setAssociateId(order.getMemberId());
        }
        appeal.setType(appealApply.getType());
        appeal.setOrder(order);
        appeal.setRemark(appealApply.getRemark());
        if (!StringUtils.isEmpty(appealApply.getImgUrls())) {
            appeal.setImgUrls(appealApply.getImgUrls());
        }
        Appeal appeal1 = appealService.save(appeal);
        if (appeal1 != null) {
            remindService.sendInfo(memberService.findOne(infoMemberId), order, RemindType.APPEAL);
            return MessageResult.success(msService.getMessage("APPEAL_SUCCESS"));
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    @RequestMapping(value = "overview")
    @ApiOperation(value = "成交历史总览")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult overview(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        OtcOrderOverview overview = orderService.countMemberOrderOverview(user.getId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("allBuyCount", overview.getSuccessBuyCount());
        map.put("allSellCount", overview.getSuccessSellCount());
        long successOrder30Day = overview.getSuccessBuyCount30() + overview.getSuccessSellCount30();
        long failOrder30Day = overview.getFailBuyCount30() + overview.getFailSellCount30();
        BigDecimal allOrderCount30Day = BigDecimal.valueOf(successOrder30Day + failOrder30Day);
        BigDecimal allBuyCount30Day = BigDecimal.valueOf(overview.getSuccessBuyCount30() + overview.getFailBuyCount30());
        map.put("successOrder30Day", successOrder30Day);
        if (allOrderCount30Day.compareTo(BigDecimal.ZERO) > 0) {
            map.put("rate30Day", BigDecimal.valueOf(successOrder30Day).divide(allOrderCount30Day, 4, RoundingMode.HALF_UP));
        } else {
            map.put("rate30Day", BigDecimal.ONE);
        }
        if (allBuyCount30Day.compareTo(BigDecimal.ZERO) > 0) {
            map.put("rateBuy30Day", BigDecimal.valueOf(overview.getSuccessBuyCount30()).divide(allBuyCount30Day, 4, RoundingMode.HALF_UP));
        } else {
            map.put("rateBuy30Day", BigDecimal.ONE);
        }
        return success(map);
    }

    private MessageResult sendSms(Advertise advertise, String userName) throws Exception {
        MessageResult result;
        Member member = advertise.getMember();
        Country country = member.getCountry();
        if ("sendcloud".equalsIgnoreCase(driverName)) {
            if ("86".equals(country.getAreaCode())) {
                result = smsProvider.sendTemplateMessage(39145, member.getMobilePhone(), false, ImmutableMap.of("id", String.valueOf(advertise.getId()), "account", userName));
            } else {
                result = smsProvider.sendTemplateMessage(39147, country.getAreaCode() + member.getMobilePhone(), true, ImmutableMap.of("id", String.valueOf(advertise.getId()), "account", userName));
            }
        } else if (driverName.equalsIgnoreCase("two_five_three")) {
            result = smsProvider.sendSingleMessage(country.getAreaCode() + member.getMobilePhone(), String.format(msService.getMessage("NEW_ORDER_SMS_REMIND"), advertise.getId(), userName));
        } else {
            if (country.getAreaCode().equals("86")) {
                result = smsProvider.sendSingleMessage(member.getMobilePhone(), String.format(msService.getMessage("NEW_ORDER_SMS_REMIND"), advertise.getId(), userName));
            } else {
                result = smsProvider.sendSingleMessage(country.getAreaCode() + member.getMobilePhone(), String.format(msService.getMessage("NEW_ORDER_SMS_REMIND"), advertise.getId(), userName));
            }
        }
        return result;
    }

    /**
     * 一键买币
     *
     * @param coinId
     * @param money
     * @param money
     * @param user
     * @return
     * @throws InformationExpiredException
     */
    @RequestMapping(value = "easyBuy", method = RequestMethod.POST)
    @ApiOperation(value = "一键买币")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinId", value = "币种id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "payMode", value = "买家选择的付款方式", required = false, dataType = "String"),
            @ApiImplicitParam(name = "money", value = "金额", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "amount", value = "数量", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "mode", value = "模式,0:按金额/1:按数量", required = false, dataType = "Integer")
    })
    public MessageResult easyBuy(long coinId, String payMode, BigDecimal money, BigDecimal amount,
                                 @RequestParam(value = "mode", defaultValue = "0") Integer mode,
                                 @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws InformationExpiredException {
        int cancelNum = orderService.countByCustomerIdAndStatusAndAdvertiseTypeAndCancelTimeBetween(user.getId(),OrderStatus.CANCELLED,AdvertiseType.SELL, DateUtils.addDays(DateUtil.getCurrentDate(),-1),DateUtil.getCurrentDate());
        isTrue(cancelNum<3, msService.getMessage("NOT_ALLOW_BUY"));
        Advertise advertise;
        if (mode == 0) {
            advertise = advertiseService.getAllAdvertiseForEasyBuy(user.getId(), coinId, money, payMode);
        } else {
            advertise = advertiseService.getAllAdvertiseForEasyBuyByAmount(user.getId(), coinId, amount, payMode);
        }
        if (advertise == null) {
            return MessageResult.error(msService.getMessage("NO_ADVERTISE_ERROR"));
        }
        OtcCoin otcCoin = advertise.getCoin();
        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        Member member = memberService.findOne(user.getId());
        if (member.getMemberLevel().getOrdinal() < 1) {
            return MessageResult.error(msService.getMessage("NO_REAL_NAME"));
        }
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error(msService.getMessage("CANNOT_TRADE"));
        }
        OtcWallet memberWallet = otcWalletService.findByOtcCoinAndMemberId(user.getId(), otcCoin);
        if (memberWallet == null) {
            //如果法币账户不存在新建
            OtcWallet otcWalletNew = new OtcWallet();
            Coin coin = coinService.findByUnit(otcCoin.getUnit());
            otcWalletNew.setCoin(coin);
            otcWalletNew.setIsLock(0);
            otcWalletNew.setMemberId(member.getId());
            otcWalletNew.setBalance(BigDecimal.ZERO);
            otcWalletNew.setFrozenBalance(BigDecimal.ZERO);
            otcWalletNew.setReleaseBalance(BigDecimal.ZERO);
            otcWalletNew.setVersion(0);
            memberWallet = otcWalletService.save(otcWalletNew);
            if (memberWallet == null) {
                return MessageResult.error(msService.getMessage("CREATE_WALLET_ERROR"));
            }
        }
        if (memberWallet.getIsLock() == 1) {
            return MessageResult.error(msService.getMessage("WALLET_IS_LOCK"));
        }
        isTrue(compare(money, advertise.getMinLimit()), msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString() + " CNY");
        isTrue(compare(advertise.getMaxLimit(), money), msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString() + " CNY");
        String[] pay = advertise.getPayMode().split(",");
        //获取手续费比例
        Member sellMember = memberService.findOne(advertise.getMember().getId());
        MemberGrade grade = gradeService.findOne(sellMember.getMemberGradeId());
        if (mode == 0) {
            amount = money.divide(advertise.getPrice(), otcCoin.getCoinScale(), RoundingMode.DOWN);
        } else {
            money = amount.multiply(advertise.getPrice()).setScale(2,RoundingMode.HALF_UP);
        }
        //计算手续费
        BigDecimal commission = mulRound(amount, grade.getExchangeFeeRate());

        isTrue(compare(advertise.getRemainAmount(), add(commission, amount)), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(commission);
        order.setCountry(advertise.getCountry().getZhName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(member.getRealName());
        order.setMemberId(advertise.getMember().getId());
        order.setMemberName(advertise.getMember().getUsername());
        order.setMemberRealName(advertise.getMember().getRealName());
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(amount);
        order.setPayMode(StringUtils.isEmpty(payMode) ? advertise.getPayMode() : payMode);
        order.setPrice(advertise.getPrice());
        order.setTimeLimit(advertise.getTimeLimit());
        //随机生成6位付款参考号
        order.setReferenceNumber(GeneratorUtil.getNonceNumberString(6));
        Arrays.stream(pay).forEach(x -> {
            if (String.valueOf(ALIPAY.getOrdinal()).equals(x)) {
                order.setAlipay(advertise.getMember().getAlipay());
            } else if (String.valueOf(WECHAT.getOrdinal()).equals(x)) {
                order.setWechatPay(advertise.getMember().getWechatPay());
            } else if (String.valueOf(BANK.getOrdinal()).equals(x)) {
                order.setBankInfo(advertise.getMember().getBankInfo());
            }
        });
        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), add(commission, amount))) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        Order order1 = orderService.saveOrder(order);
        if (order1 != null) {
            sendMessageAndSaveContent(user, advertise, order1);
            MessageResult result = MessageResult.success(msService.getMessage("CREATE_ORDER_SUCCESS"));
            result.setData(order1.getOrderSn());
            return result;
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    /**
     * 一键卖币
     *
     * @param coinId
     * @param amount
     * @param user
     * @return
     * @throws InformationExpiredException
     */
    @RequestMapping(value = "easySell")
    @ApiOperation(value = "一键卖币")
    @Transactional(rollbackFor = Exception.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinId", value = "币种id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "money", value = "金额", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "amount", value = "数量", required = false, dataType = "BigDecimal"),
            @ApiImplicitParam(name = "mode", value = "模式,0:按金额/1:按数量", required = false, dataType = "Integer")
    })
    public MessageResult easySell(long coinId, BigDecimal amount, BigDecimal money,
                                  @RequestParam(value = "mode", defaultValue = "0") Integer mode,
                                  @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) throws InformationExpiredException {
        log.info("登陆用户的信息={}", user);
        Member member = memberService.findOne(user.getId());
        String payMode = "X";
        String payMode2 = "X";
        String payMode3 = "X";
        if (member.getAlipay() != null) {
            payMode = "0";
        }
        if (member.getWechatPay() != null) {
            payMode2 = "1";
        }
        if (member.getBankInfo() != null) {
            payMode3 = "2";
        }
        Advertise advertise;
        if (mode == 0) {
            advertise = advertiseService.getAllAdvertiseForEasySell(user.getId(), coinId, money, payMode, payMode2, payMode3);
        } else {
            advertise = advertiseService.getAllAdvertiseForEasySellByAmount(user.getId(), coinId, amount, payMode, payMode2, payMode3);
        }
        if (advertise == null) {
            return MessageResult.error(msService.getMessage("NO_ADVERTISE_ERROR"));
        }
        OtcCoin otcCoin = advertise.getCoin();
        if (otcCoin.getId() != coinId) {
            return MessageResult.error(msService.getMessage("PARAMETER_ERROR"));
        }
        if (mode == 0) {
            amount = money.divide(advertise.getPrice(), otcCoin.getCoinScale(), RoundingMode.DOWN);
        } else {
            money = amount.multiply(advertise.getPrice()).setScale(2,RoundingMode.HALF_UP);
        }
        isTrue(compare(money, advertise.getMinLimit()), msService.getMessage("MONEY_MIN") + advertise.getMinLimit().toString() + " CNY");
        isTrue(compare(advertise.getMaxLimit(), money), msService.getMessage("MONEY_MAX") + advertise.getMaxLimit().toString() + " CNY");
        isTrue(compare(advertise.getRemainAmount(), amount), msService.getMessage("AMOUNT_NOT_ENOUGH"));
        if (member.getMemberLevel().getOrdinal() < 1) {
            return MessageResult.error(msService.getMessage("NO_REAL_NAME"));
        }
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error(msService.getMessage("CANNOT_TRADE"));
        }
        OtcWallet wallet = otcWalletService.findByOtcCoinAndMemberId(user.getId(), otcCoin);
        if (wallet == null) {
            return MessageResult.error(msService.getMessage("NOT_OTC_WALLET"));
        }
        if (wallet.getIsLock() == 1) {
            return MessageResult.error(msService.getMessage("WALLET_IS_LOCK"));
        }
        isTrue(wallet.getIsLock() == 0, msService.getMessage("WALLET_IS_LOCK"));
        isTrue(compare(wallet.getBalance(), amount), msService.getMessage("INSUFFICIENT_BALANCE"));
        //获取手续费比例
        MemberGrade grade = gradeService.findOne(advertise.getMember().getMemberGradeId());
        log.info("商家的等级grade={}", grade);
        //计算手续费
        BigDecimal commission = mulRound(amount, grade.getOtcFeeRate());
        log.info("该手续费commission={}", commission);
        Order order = new Order();
        order.setStatus(OrderStatus.NONPAYMENT);
        order.setAdvertiseId(advertise.getId());
        order.setAdvertiseType(advertise.getAdvertiseType());
        order.setCoin(otcCoin);
        order.setCommission(commission);
        order.setCountry(advertise.getCountry().getZhName());
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerRealName(member.getRealName());
        order.setMemberId(advertise.getMember().getId());
        order.setMemberName(advertise.getMember().getUsername());
        order.setMemberRealName(advertise.getMember().getRealName());
        order.setMaxLimit(advertise.getMaxLimit());
        order.setMinLimit(advertise.getMinLimit());
        order.setMoney(money);
        order.setNumber(amount);
        order.setPayMode(advertise.getPayMode());
        order.setPrice(advertise.getPrice());
        //随机生成6位付款参考号
        order.setReferenceNumber(GeneratorUtil.getNonceNumberString(6));
        order.setTimeLimit(advertise.getTimeLimit());
        String[] pay = advertise.getPayMode().split(",");
        MessageResult result = MessageResult.error(msService.getMessage("CREATE_ORDER_SUCCESS"));
        Arrays.stream(pay).forEach(x -> {
            if (ALIPAY.getOrdinal() == Integer.valueOf(x)) {
                if (member.getAlipay() != null) {
                    result.setCode(0);
                    order.setAlipay(member.getAlipay());
                }
            } else if (WECHAT.getOrdinal() == Integer.valueOf(x)) {
                if (member.getWechatPay() != null) {
                    result.setCode(0);
                    order.setWechatPay(member.getWechatPay());
                }
            } else if (BANK.getOrdinal() == Integer.valueOf(x)) {
                if (member.getBankInfo() != null) {
                    result.setCode(0);
                    order.setBankInfo(member.getBankInfo());
                }
            }
        });
        isTrue(result.getCode() == 0, msService.getMessage("AT_LEAST_SUPPORT_PAY"));
        if (!advertiseService.updateAdvertiseAmountForBuy(advertise.getId(), amount)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        //冻结用户余额=卖出数量
        if (!(otcWalletService.freezeBalance(wallet, amount).getCode() == 0)) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        Order order1 = orderService.saveOrder(order);
        if (order1 != null) {
            sendMessageAndSaveContent(user, advertise, order1);
            result.setData(order1.getOrderSn());
            return result;
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }
}
