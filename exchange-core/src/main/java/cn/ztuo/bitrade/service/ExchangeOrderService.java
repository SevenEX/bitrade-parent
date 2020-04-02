package cn.ztuo.bitrade.service;


import cn.ztuo.bitrade.util.GeneratorUtil;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.PromotionRewardType;
import cn.ztuo.bitrade.constant.RewardRecordType;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.ExchangeOrderRepository;
import cn.ztuo.bitrade.dao.OrderDetailAggregationRepository;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.QExchangeOrder;
import cn.ztuo.bitrade.enums.PaymentType;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import cn.ztuo.bitrade.util.DateUtil;

import cn.ztuo.bitrade.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class ExchangeOrderService extends BaseService {
    @Autowired
    private ExchangeOrderRepository exchangeOrderRepository;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private OrderDetailAggregationRepository orderDetailAggregationRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Value("${channel.enable:false}")
    private Boolean channelEnable;
    @Value("${channel.exchange-rate:0.00}")
    private BigDecimal channelExchangeRate;
    @Autowired
    private PlatformTransactionService platformTransactionService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private LeverWalletService leverWalletService;
    @Autowired
    private LeverCoinService leverCoinService;
    @Autowired
    private PaymentHistoryService paymentHistoryService;

    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberGradeService gradeService ;
    @Autowired
    private RobotTransactionService robotTransactionService ;

    public Page<ExchangeOrder> findAll(Predicate predicate, Pageable pageable) {
        return exchangeOrderRepository.findAll(predicate, pageable);
    }


    /**
     * 添加委托订单
     *
     * @param memberId
     * @param order
     * @return
     */
    @Transactional
    public MessageResult addOrder(Long memberId, ExchangeOrder order) {
        order.setTime(Calendar.getInstance().getTimeInMillis());
        if(order.getType()==ExchangeOrderType.CHECK_FULL_STOP){
            order.setStatus(ExchangeOrderStatus.WAITING_TRIGGER);
        }else {
            order.setStatus(ExchangeOrderStatus.TRADING);
        }
        order.setTradedAmount(BigDecimal.ZERO);
        order.setOrderId(GeneratorUtil.getOrderId("E"));
        log.info("add order:{}", order);
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getBaseSymbol(), memberId);
            if(wallet.getIsLock().equals(BooleanEnum.IS_TRUE)){
                return MessageResult.error(msService.getMessage("WALLET_LOCKED"));
            }
            BigDecimal turnover;
            if (order.getType() == ExchangeOrderType.MARKET_PRICE) {
                turnover = order.getAmount();
            } else {
                turnover = order.getAmount().multiply(order.getPrice());
            }
            MessageResult result = memberWalletService.freezeBalance(wallet, turnover);
            if (result.getCode() != 0) {
                return MessageResult.error(500,msService.getMessage("INSUFFICIENT_COIN") + order.getBaseSymbol());
            }
        } else if (order.getDirection() == ExchangeOrderDirection.SELL) {
            MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getCoinSymbol(), memberId);
            if(wallet.getIsLock().equals(BooleanEnum.IS_TRUE)){
                return MessageResult.error(msService.getMessage("WALLET_LOCKED"));
            }
            MessageResult result = memberWalletService.freezeBalance(wallet, order.getAmount());
            if (result.getCode() != 0) {
                return MessageResult.error(500, msService.getMessage("INSUFFICIENT_COIN") + order.getCoinSymbol());
            }
        }
        order = exchangeOrderRepository.saveAndFlush(order);
        if (order != null) {
            return MessageResult.success(msService.getMessage("SUCCESS"));
        } else {
            return MessageResult.error(500, msService.getMessage("ERROR"));
        }
    }

    /**
     * @param uid
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<ExchangeOrder> findHistory(Long uid, String symbol, int pageNum, int pageSize,BooleanEnum marginTrade) {
        Sort orders = Sort.by(new Sort.Order(Sort.Direction.DESC, "time"));
        PageRequest pageRequest = PageRequest.of(pageNum-1, pageSize, orders);
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, true));
        specification.add(Restrictions.eq("memberId", uid, true));
        List<ExchangeOrderStatus> list = new ArrayList<>();
        list.add(ExchangeOrderStatus.CANCELED);
        list.add(ExchangeOrderStatus.COMPLETED);
        list.add(ExchangeOrderStatus.OVERTIMED);
        specification.add(Restrictions.in("status", list, false));
        specification.add(Restrictions.eq("marginTrade",marginTrade,true));
        return exchangeOrderRepository.findAll(specification, pageRequest);
    }

    /**
     * 查询当前交易中的委托
     *
     * @param uid
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<ExchangeOrder> findCurrent(Long uid, String symbol, int pageNo, int pageSize,BooleanEnum marginTrade) {
        Sort orders = Sort.by(new Sort.Order(Sort.Direction.DESC, "time"));
        PageRequest pageRequest = PageRequest.of(pageNo-1, pageSize, orders);
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, true));
        specification.add(Restrictions.eq("memberId", uid, false));
        List<ExchangeOrderStatus> list = new ArrayList<>();
        list.add(ExchangeOrderStatus.TRADING);
        list.add(ExchangeOrderStatus.WAITING_TRIGGER);
        specification.add(Restrictions.in("status", list, false));
        specification.add(Restrictions.eq("marginTrade",marginTrade,true));
        return exchangeOrderRepository.findAll(specification, pageRequest);
    }


    /**
     * 处理交易匹配
     *
     * @param trade
     * @param secondReferrerAward 二级推荐人是否返回佣金 true 返回佣金
     * @param usdRateMap
     * @param seFeeScale
     * @return
     * @throws Exception
     */
    @Transactional
    public MessageResult processExchangeTrade(ExchangeTrade trade, boolean secondReferrerAward, HashMap<String, BigDecimal> usdRateMap, int seFeeScale) throws Exception {
        log.info("processExchangeTrade,trade = {}", trade);
        if (trade == null || trade.getBuyOrderId() == null || trade.getSellOrderId() == null) {
            return MessageResult.error(500, msService.getMessage("TRADE_NULL"));
        }
        ExchangeOrder buyOrder = exchangeOrderRepository.findByOrderId(trade.getBuyOrderId());
        ExchangeOrder sellOrder = exchangeOrderRepository.findByOrderId(trade.getSellOrderId());
        if (buyOrder == null || sellOrder == null) {
            log.error("order not found");
            return MessageResult.error(500,  msService.getMessage("ORDER_NOT_FOUND"));
        }
        if(trade.getBuyTurnover().compareTo(trade.getSellTurnover()) != 0){
            platformTransactionService.add(trade.getBuyTurnover().subtract(trade.getSellTurnover()),3,32,trade.getBuyOrderId());
        }
        //获取手续费率 买卖人的等级
        MemberGrade buyMemberGrade  ,sellMemberGrade;
        Member buyMember = memberService.findOne(buyOrder.getMemberId());
        Member sellMember = memberService.findOne(sellOrder.getMemberId());
        if(buyMember.getMemberGradeId().equals(sellMember.getMemberGradeId())){
            buyMemberGrade = sellMemberGrade = gradeService.findOne(buyMember.getMemberGradeId());
        }else {
            buyMemberGrade = gradeService.findOne(buyMember.getMemberGradeId());
            sellMemberGrade = gradeService.findOne(sellMember.getMemberGradeId());
        }
        //根据memberId锁表，防止死锁
        memberService.selectMemberWalletForUpdate(buyOrder.getMemberId());
        if(!buyOrder.getMemberId().equals( sellOrder.getMemberId())) {
            memberService.selectMemberWalletForUpdate(sellOrder.getMemberId());
        }
        if(trade.getIsBuyerMaker() == null) {
            trade.setIsBuyerMaker(buyOrder.getTime() < sellOrder.getTime());
        }
        //处理买入订单
        processOrder(buyOrder, trade, buyMemberGrade, secondReferrerAward, buyMember, sellMember, trade.getIsBuyerMaker(), usdRateMap, seFeeScale);
        //处理卖出订单
        processOrder(sellOrder, trade, sellMemberGrade, secondReferrerAward, sellMember, buyMember, !trade.getIsBuyerMaker(), usdRateMap, seFeeScale);
        return MessageResult.success(msService.getMessage("PROCESS_SUCCESS"));
    }

    /**
     * 对发生交易的委托处理相应的钱包
     *
     * @param order               委托订单
     * @param trade               交易详情
     * @param grade               手续费
     * @param secondReferrerAward 二级推荐人是否返佣
     * @param usdRateMap
     * @param seFeeScale
     * @return
     */
    public void processOrder(ExchangeOrder order, ExchangeTrade trade, MemberGrade grade,
                             boolean secondReferrerAward, Member member, Member dealMember, boolean isMaker, HashMap<String, BigDecimal> usdRateMap, int seFeeScale) {
        Long time = Calendar.getInstance().getTimeInMillis();
        //添加成交详情
        ExchangeOrderDetail orderDetail = new ExchangeOrderDetail();
        orderDetail.setOrderId(order.getOrderId());
        orderDetail.setTime(time);
        orderDetail.setPrice(trade.getPrice());
        orderDetail.setAmount(trade.getAmount());
        orderDetail.setMemberId(member.getId());
        orderDetail.setDealMemberId(dealMember.getId());

        BigDecimal incomeCoinAmount, turnover, outcomeCoinAmount;
        BigDecimal feeRatio;
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            feeRatio = isMaker ? grade.getExchangeMakerFeeRate() : grade.getExchangeFeeRate();
            turnover = trade.getBuyTurnover();
        } else {
            feeRatio = isMaker ? grade.getExchangeMakerFeeRate() : grade.getExchangeFeeRate();
            turnover = trade.getSellTurnover();
        }
        orderDetail.setTurnover(turnover);
        //手续费，买入订单收取coin,卖出订单收取baseCoin
        BigDecimal fee;
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            fee = trade.getAmount().multiply(feeRatio);
        } else {
            fee = turnover.multiply(feeRatio);
        }
        String incomeSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getCoinSymbol() : order.getBaseSymbol();
        String feeSymbol = incomeSymbol;
        boolean seFeeFlag = false;
        if(member.getSeFeeSwitch() != null && member.getSeFeeSwitch()) {
            BigDecimal feeSymbolUsdRate = usdRateMap.get(feeSymbol);
            if(feeSymbolUsdRate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal seUsdRate = usdRateMap.get("SE");
                if(seUsdRate.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal feeSE = feeSymbolUsdRate.multiply(fee).multiply(grade.getSeDiscountRate()).divide(seUsdRate, seFeeScale, RoundingMode.HALF_UP);
                    if(feeSE.compareTo(BigDecimal.ZERO) > 0) {
                        MemberWallet seWallet = memberWalletService.findByCoinUnitAndMemberId("SE", member.getId());
                        if(seWallet.getBalance().compareTo(feeSE) >= 0) {
                            seFeeFlag = true;
                            feeSymbol = "SE";
                            fee = feeSE;
                            memberWalletService.decreaseBalance(seWallet.getId(), feeSE);
                        }
                    }
                }
            }
        }
        orderDetail.setFee(fee);
        orderDetail.setFeeSymbol(feeSymbol);
        exchangeOrderDetailService.save(orderDetail);

        //增加回报的可用的币,处理账户增加的币种，买入的时候获得交易币，卖出的时候获得基币
        if(!seFeeFlag) {
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                incomeCoinAmount = trade.getAmount().subtract(fee);
            } else {
                incomeCoinAmount = turnover.subtract(fee);
            }
        }
        else {
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                incomeCoinAmount = trade.getAmount();
            } else {
                incomeCoinAmount = turnover;
            }
        }
        //扣除付出的币，买入的时候算成交额，卖出的算成交量
        String outcomeSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getBaseSymbol() : order.getCoinSymbol();
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            outcomeCoinAmount = turnover;
        } else {
            outcomeCoinAmount = trade.getAmount();
        }

        /**
         * 聚合币币交易订单手续费明细存入mongodb
         */
        OrderDetailAggregation aggregation = new OrderDetailAggregation();
        aggregation.setType(OrderTypeEnum.EXCHANGE);

        aggregation.setFee(fee.doubleValue());
        aggregation.setTime(orderDetail.getTime()/1000);
        aggregation.setDirection(order.getDirection());
        aggregation.setOrderId(order.getOrderId());
        aggregation.setAmount(incomeCoinAmount.doubleValue());
        aggregation.setUnit(feeSymbol);

        aggregation.setMemberId(member.getId());
        aggregation.setUsername(member.getUsername());
        aggregation.setRealName(member.getRealName());
        orderDetailAggregationRepository.save(aggregation);

        if(order.getMarginTrade() != null && order.getMarginTrade().equals(BooleanEnum.IS_TRUE)){
            Coin incomeCoin=coinService.findByUnit(incomeSymbol);
            Coin outcomeCoin=coinService.findByUnit(outcomeSymbol);
            LeverCoin leverCoin=leverCoinService.getBySymbol(order.getSymbol());
            LeverWallet incomeWallet=leverWalletService.findByMemberIdAndCoinAndLeverCoin(order.getMemberId(),incomeCoin,leverCoin);
            LeverWallet outcomeWallet=leverWalletService.findByMemberIdAndCoinAndLeverCoin(order.getMemberId(),outcomeCoin,leverCoin);
            incomeWallet.setBalance(incomeWallet.getBalance().add(incomeCoinAmount));
            outcomeWallet.setFrozenBalance(outcomeWallet.getFrozenBalance().subtract(outcomeCoinAmount));
            leverWalletService.save(incomeWallet);
            leverWalletService.save(outcomeWallet);
        }else {
            //查询用户付出的钱包信息
            MemberWallet outcomeWallet = memberWalletService.findByCoinUnitAndMemberId(outcomeSymbol, order.getMemberId());
            //去掉冻结金额
            memberWalletService.decreaseFrozen(outcomeWallet.getId(), outcomeCoinAmount);
            //增加用户的交易得到的币
            MemberWallet incomeWallet = memberWalletService.findByCoinUnitAndMemberId(incomeSymbol, order.getMemberId());
            memberWalletService.increaseBalance(incomeWallet.getId(), incomeCoinAmount);
        }
        if(order.getMarginTrade() != null && order.getMarginTrade().equals(BooleanEnum.IS_TRUE)){
            //杠杆交易，计入paymentHistory
            //增加入资金记录
            LeverCoin leverCoin=leverCoinService.getBySymbol(order.getSymbol());
            PaymentHistory paymentHistory=new PaymentHistory();
            paymentHistory.setLeverCoin(leverCoin);
            paymentHistory.setCoin(coinService.findByUnit(incomeSymbol));
            paymentHistory.setMemberId(order.getMemberId());
            paymentHistory.setPaymentType(PaymentType.LEVER_EXCHAGE);
            paymentHistory.setAmount(incomeCoinAmount);
            paymentHistoryService.save(paymentHistory);
            //增加出资金记录
            PaymentHistory paymentHistory2=new PaymentHistory();
            paymentHistory2.setLeverCoin(leverCoin);
            paymentHistory2.setCoin(coinService.findByUnit(outcomeSymbol));
            paymentHistory2.setMemberId(order.getMemberId());
            paymentHistory2.setPaymentType(PaymentType.LEVER_EXCHAGE);
            paymentHistory2.setAmount(outcomeCoinAmount.negate());
            paymentHistoryService.save(paymentHistory2);
        }else{
            if(order.getOrderResource()!=ExchangeOrderResource.ROBOT) {
                //普通币币交易,增加入资金记录
                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(incomeCoinAmount);
                transaction.setSymbol(incomeSymbol);
                transaction.setAddress("");
                transaction.setMemberId(order.getMemberId());

                transaction.setType(TransactionType.EXCHANGE);
                transaction.setFee(fee);
                transaction.setFeeUnit(feeSymbol);
                transaction.setCreateTime(new Date());
                transactionService.save(transaction);

                //增加出资金记录
                MemberTransaction transaction2 = new MemberTransaction();
                transaction2.setAmount(outcomeCoinAmount.negate());
                transaction2.setSymbol(outcomeSymbol);
                transaction2.setAddress("");
                transaction2.setMemberId(order.getMemberId());
                transaction2.setType(TransactionType.EXCHANGE);
                transaction2.setFee(BigDecimal.ZERO);
                transaction2.setCreateTime(new Date());
                transactionService.save(transaction2);
            }else {
                RobotTransaction robotTransaction = new RobotTransaction();
                robotTransaction.setAmount(incomeCoinAmount);
                robotTransaction.setSymbol(incomeSymbol);
                robotTransaction.setMemberId(order.getMemberId());
                robotTransaction.setType(TransactionType.EXCHANGE);
                robotTransaction.setFee(fee);
                robotTransaction.setCreateTime(new Date());
                robotTransactionService.save(robotTransaction);
                //增加出资金记录
                RobotTransaction robotTransaction2 = new RobotTransaction();
                robotTransaction2.setAmount(outcomeCoinAmount.negate());
                robotTransaction2.setSymbol(outcomeSymbol);
                robotTransaction2.setMemberId(order.getMemberId());
                robotTransaction2.setType(TransactionType.EXCHANGE);
                robotTransaction2.setFee(BigDecimal.ZERO);
                robotTransaction2.setCreateTime(new Date());
                robotTransactionService.save(robotTransaction2);
            }
        }
        try {
            promoteReward(fee, member, feeSymbol, secondReferrerAward,order);
            if(channelEnable && member.getChannelId() > 0 && channelExchangeRate.compareTo(BigDecimal.ZERO) > 0){
                //处理渠道返佣
                processChannelReward(member,feeSymbol,fee);
            }
        } catch (Exception e) {
            log.info("发放币币交易推广手续费佣金出错e={}",e);
        }
    }

    public List<ExchangeOrderDetail> getAggregation(String orderId) {
        return exchangeOrderDetailService.findAllByOrderId(orderId);
    }


    /**
     * 渠道币币交易返佣
     * @param member
     * @param symbol
     * @param fee
     */
    public void processChannelReward(Member member,String symbol,BigDecimal fee){
        MemberWallet channelWallet =  memberWalletService.findByCoinUnitAndMemberId(symbol,member.getChannelId());
        if(channelWallet != null && fee.compareTo(BigDecimal.ZERO) > 0 ){
            BigDecimal amount = fee.multiply(channelExchangeRate);
            memberWalletService.increaseBalance(channelWallet.getId(),amount);
            MemberTransaction memberTransaction = new MemberTransaction();
            memberTransaction.setAmount(amount);
            memberTransaction.setFee(BigDecimal.ZERO);
            memberTransaction.setMemberId(member.getChannelId());
            memberTransaction.setSymbol(symbol);
            memberTransaction.setType(TransactionType.CHANNEL_AWARD);
            transactionService.save(memberTransaction);
        }
    }

    /**
     * 交易手续费返佣金
     *
     * @param fee                 手续费
     * @param member              订单拥有者
     * @param incomeSymbol        币种
     * @param secondReferrerAward 二级推荐人是否返佣控制
     */
    public void promoteReward(BigDecimal fee, Member member, String incomeSymbol, boolean secondReferrerAward,ExchangeOrder order) {
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(PromotionRewardType.EXCHANGE_TRANSACTION);
        if (rewardPromotionSetting != null && member.getInviterId() != null) {
           // if (!(DateUtil.diffDays(new Date(), member.getRegistrationTime()) > TrewardPromotionSetting.getEffectiveime())) {
                Member member1 = memberService.findOne(member.getInviterId());
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(incomeSymbol, member1.getId());
                JSONObject jsonObject = JSONObject.parseObject(rewardPromotionSetting.getInfo());
                BigDecimal reward = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(jsonObject.getBigDecimal("one")), 8);
            if (reward.compareTo(BigDecimal.ZERO) > 0) {
                    //memberWallet.setBalance(BigDecimalUtils.add(memberWallet.getBalance(), reward));
                    memberWalletService.increaseBalance(memberWallet.getId(), reward);
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setAmount(reward);
                    memberTransaction.setFee(BigDecimal.ZERO);
                    memberTransaction.setMemberId(member1.getId());
                    memberTransaction.setSymbol(incomeSymbol);
                    memberTransaction.setType(TransactionType.PROMOTION_AWARD);
                    transactionService.save(memberTransaction);
                    RewardRecord rewardRecord1 = new RewardRecord();
                    rewardRecord1.setAmount(reward);
                    rewardRecord1.setCoin(memberWallet.getCoin());
                    rewardRecord1.setMember(member1);
                    rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
                    rewardRecord1.setType(RewardRecordType.PROMOTION);
                    rewardRecord1.setOrderId(order.getOrderId());
                    rewardRecord1.setOrderMember(member);

                    rewardRecordService.save(rewardRecord1);
                }

                // 控制推荐人推荐是否返佣 等于false是二级推荐人不返佣
                if (secondReferrerAward == false) {
                    log.info("控制字段 : secondReferrerAward ={} , 跳过二级推荐人返佣", secondReferrerAward);
                    return;
                }
                if (member1.getInviterId() != null && !(DateUtil.diffDays(new Date(), member1.getRegistrationTime()) > rewardPromotionSetting.getEffectiveTime())) {
                    Member member2 = memberService.findOne(member1.getInviterId());
                    MemberWallet memberWallet1 = memberWalletService.findByCoinUnitAndMemberId(incomeSymbol, member2.getId());
                    BigDecimal reward1 = BigDecimalUtils.mulRound(fee, BigDecimalUtils.getRate(jsonObject.getBigDecimal("two")), 8);
                    if (reward1.compareTo(BigDecimal.ZERO) > 0) {
                        //memberWallet1.setBalance(BigDecimalUtils.add(memberWallet1.getBalance(), reward));
                        memberWalletService.increaseBalance(memberWallet1.getId(), reward1);
                        MemberTransaction memberTransaction = new MemberTransaction();
                        memberTransaction.setAmount(reward1);
                        memberTransaction.setFee(BigDecimal.ZERO);
                        memberTransaction.setMemberId(member2.getId());
                        memberTransaction.setSymbol(incomeSymbol);
                        memberTransaction.setType(TransactionType.PROMOTION_AWARD);
                        transactionService.save(memberTransaction);
                        RewardRecord rewardRecord1 = new RewardRecord();
                        rewardRecord1.setAmount(reward1);
                        rewardRecord1.setCoin(memberWallet1.getCoin());
                        rewardRecord1.setMember(member2);
                        rewardRecord1.setRemark(rewardPromotionSetting.getType().getCnName());
                        rewardRecord1.setType(RewardRecordType.PROMOTION);
                        rewardRecord1.setOrderId(order.getOrderId());
                        rewardRecord1.setOrderMember(member);
                        rewardRecordService.save(rewardRecord1);
                    }
                }
           // }
        }
    }

    /**
     * 查询所有未完成的挂单
     *
     * @param symbol 交易对符号
     * @return
     */
    public List<ExchangeOrder> findAllTradingOrderBySymbol(String symbol) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "time"));
    }
    @Override
    public List<ExchangeOrder> findAll() {
        return exchangeOrderRepository.findAll();
    }

    public ExchangeOrder findOne(String id) {
        return exchangeOrderRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public PageResult<ExchangeOrder> queryWhereOrPage(List<Predicate> predicates, Integer pageNo, Integer pageSize) {
        List<ExchangeOrder> list;
        JPAQuery<ExchangeOrder> jpaQuery = queryFactory.selectFrom(QExchangeOrder.exchangeOrder);
        if (predicates != null) {
            jpaQuery.where(predicates.toArray(new BooleanExpression[predicates.size()]));
        }
        jpaQuery.orderBy(QExchangeOrder.exchangeOrder.time.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        PageResult<ExchangeOrder> result = new PageResult<>(list, jpaQuery.fetchCount());
        result.setNumber(pageNo);
        result.setSize(pageSize);
        return result;
    }

    /**
     * 订单交易完成
     *
     * @param orderId
     * @return
     */
    @Transactional
    public MessageResult orderCompleted(String orderId, BigDecimal tradedAmount, BigDecimal turnover) throws Exception{
        ExchangeOrder order = exchangeOrderRepository.getOrderForUpdate(orderId).orElse(null);
        if (order == null) {
            return MessageResult.error(msService.getMessage("ORDER_NOT_EXISTS"));
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING && order.getStatus() != ExchangeOrderStatus.WAITING_TRIGGER) {
            return MessageResult.error(500, "(" + orderId + ")" + msService.getMessage("INVALID_ORDER"));
        }
        order.setTradedAmount(tradedAmount);
        order.setTurnover(turnover);
        order.setStatus(ExchangeOrderStatus.COMPLETED);
        order.setCompletedTime(Calendar.getInstance().getTimeInMillis());
        exchangeOrderRepository.saveAndFlush(order);
        //处理用户钱包,对冻结作处理，剩余成交额退回
        memberService.selectMemberWalletForUpdate(order.getMemberId());
        orderRefund(order, tradedAmount, turnover);
        return MessageResult.success(msService.getMessage("SUCCESS"));
    }

    /**
     * 委托退款，如果取消订单或成交完成有剩余
     *
     * @param order
     * @param tradedAmount
     * @param turnover
     */
    public void orderRefund(ExchangeOrder order, BigDecimal tradedAmount, BigDecimal turnover) {
        //下单时候冻结的币，实际成交应扣的币
        BigDecimal frozenBalance, dealBalance;
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            if (order.getType() != ExchangeOrderType.MARKET_PRICE) {
                frozenBalance = order.getAmount().multiply(order.getPrice());
            } else {
                frozenBalance = order.getAmount();
            }
            dealBalance = turnover;
        } else {
            frozenBalance = order.getAmount();
            dealBalance = tradedAmount;
        }
        //减少付出的冻结的币
        BigDecimal refundAmount = frozenBalance.subtract(dealBalance);
        log.info("退币：" + refundAmount);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            String coinSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getBaseSymbol() : order.getCoinSymbol();
            //杠杆交易订单，需要退回杠杆钱包
            if(order.getMarginTrade() != null && order.getMarginTrade().equals(BooleanEnum.IS_TRUE)){
                LeverCoin leverCoin=leverCoinService.getBySymbol(order.getSymbol());
                Coin coin=coinService.findByUnit(coinSymbol);
                LeverWallet leverWallet=leverWalletService.findByMemberIdAndLeverCoinAndCoinAndIsLock(order.getMemberId(),
                        leverCoin,coin,BooleanEnum.IS_FALSE);
                leverWalletService.thawBalance(leverWallet,refundAmount);
            }else{
                //非杠杆交易订单，退回普通钱包
                MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(coinSymbol, order.getMemberId());
                MessageResult mr = memberWalletService.thawBalance(wallet, refundAmount);
                if(mr.getCode() != 0) {
                    log.error("退回冻结资金失败,order={},tradedAmount={},turnover={}", order, tradedAmount, turnover);
                }
            }
        }
    }

    /**
     * 取消订单
     *
     * @param orderId 订单编号
     * @return
     */
    @Transactional
    public MessageResult orderCanceled(String orderId, BigDecimal tradedAmount, BigDecimal turnover) throws Exception{
        ExchangeOrder order = exchangeOrderRepository.getOrderForUpdate(orderId).orElse(null);
        if (order == null) {
            return MessageResult.error(msService.getMessage("ORDER_NOT_EXISTS"));
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING && order.getStatus() != ExchangeOrderStatus.WAITING_TRIGGER) {
            return MessageResult.error(500, msService.getMessage("ORDER_NOT_IN_TRADING"));
        }
        order.setTradedAmount(tradedAmount);
        order.setTurnover(turnover);
        order.setStatus(ExchangeOrderStatus.CANCELED);
        order.setCanceledTime(Calendar.getInstance().getTimeInMillis());
        //未成交的退款
        //根据memberId锁表，防止死锁
        memberService.selectMemberWalletForUpdate(order.getMemberId());
        orderRefund(order, tradedAmount, turnover);
        return MessageResult.success();
    }


    /**
     * 获取某交易对当日已取消次数
     *
     * @param uid
     * @param symbol
     * @return
     */
    public long findTodayOrderCancelTimes(Long uid, String symbol) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.CANCELED, false));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTick = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        long endTick = calendar.getTimeInMillis();
        specification.add(Restrictions.gte("canceledTime", startTick, false));
        specification.add(Restrictions.lt("canceledTime", endTick, false));
        return exchangeOrderRepository.count(specification);
    }

    /**
     * 查询当前正在交易的订单数量
     *
     * @param uid
     * @param symbol
     * @return
     */
    public long findCurrentTradingCount(Long uid, String symbol) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.count(specification);
    }

    public long findCurrentTradingCount(Long uid, String symbol, ExchangeOrderDirection direction) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("memberId", uid, false));
        specification.add(Restrictions.eq("direction", direction, false));
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        return exchangeOrderRepository.count(specification);
    }

    public List<ExchangeOrder> findOvertimeOrder(String symbol, int maxTradingTime) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("status", ExchangeOrderStatus.TRADING, false));
        specification.add(Restrictions.eq("symbol", symbol, false));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -maxTradingTime);
        long tickTime = calendar.getTimeInMillis();
        specification.add(Restrictions.lt("time", tickTime, false));
        return exchangeOrderRepository.findAll(specification);
    }

    /**
     * 查询符合状态的订单
     *
     * @param cancelTime
     * @return
     */

    public List<ExchangeOrder> queryExchangeOrderByTimeById(long cancelTime) {
        return exchangeOrderRepository.queryExchangeOrderByTimeById(cancelTime);
    }


    /**
     * 强制取消订单,在撮合中心和数据库订单不一致的情况下使用
     * @param order
     */
    @Transactional
    public void forceCancelOrder(ExchangeOrder order) throws Exception {
        List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());
        BigDecimal tradedAmount = BigDecimal.ZERO;
        BigDecimal turnover = BigDecimal.ZERO;
        for(ExchangeOrderDetail trade:details){
            tradedAmount = tradedAmount.add(trade.getAmount());
            turnover = turnover.add(trade.getAmount().multiply(trade.getPrice()));
        }
        order.setTradedAmount(tradedAmount);
        order.setTurnover(turnover);
        if(order.isCompleted()){
            orderCompleted(order.getOrderId(),order.getTradedAmount(),order.getTurnover());
        }
        else{
            orderCanceled(order.getOrderId(),order.getTradedAmount(),order.getTurnover());
        }
    }

    public int countOrdersByMemberIdAndCreateTime(Date startTime,Date endTime){
        List<Object[]> objectList=exchangeOrderRepository.countOrdersByMemberIdAndCreateTime(startTime.getTime(),endTime.getTime());
        if(objectList!=null&&objectList.size()>0){
            return objectList.size();
        }else{
            return 0;
        }
    }

    public List<ExchangeOrder> findAllWaitingOrder(String symbol, ExchangeOrderStatus waitingTrigger) {
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        specification.add(Restrictions.eq("symbol", symbol, false));
        specification.add(Restrictions.eq("status",waitingTrigger, false));
        return exchangeOrderRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "time"));
    }

    /**
     * 个人中心历史委托
     * @param uid
     * @param symbol
     * @param type
     * @param status
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<ExchangeOrder> findPersonalHistory(Long uid, String symbol, ExchangeOrderType type, ExchangeOrderStatus status, String startTime, String endTime, ExchangeOrderDirection direction, int pageNo, int pageSize) {
        Sort orders = Sort.by(new Sort.Order(Sort.Direction.DESC, "time"));
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, orders);
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        if(StringUtils.isNotEmpty(symbol)){
            specification.add(Restrictions.eq("symbol", symbol, true));
        }
        if(type!=null&&StringUtils.isNotEmpty(type.toString())){
            specification.add(Restrictions.eq("type", type, true));
        }
        if(direction!=null&&StringUtils.isNotEmpty(direction.toString())){
            specification.add(Restrictions.eq("direction", direction, true));
        }
        specification.add(Restrictions.eq("memberId", uid, true));
        if (StringUtils.isNotEmpty(startTime)&&StringUtils.isNotEmpty(endTime)) {
            specification.add(Restrictions.gte("time", Long.valueOf(startTime), true));
            specification.add(Restrictions.lte("time", Long.valueOf(endTime), true));
        }

        if (status == null) {
            List<ExchangeOrderStatus> list = new ArrayList<>();
            list.add(ExchangeOrderStatus.CANCELED);
            list.add(ExchangeOrderStatus.COMPLETED);
            list.add(ExchangeOrderStatus.OVERTIMED);
            specification.add(Restrictions.in("status", list, false));
        } else {
            specification.add(Restrictions.eq("status", status, true));
        }
        specification.add(Restrictions.eq("marginTrade",BooleanEnum.IS_FALSE,true));

        return exchangeOrderRepository.findAll(specification, pageRequest);
    }


    /**
     * 个人中心当前委托
     *
     * @param uid
     * @param symbol
     * @param type
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<ExchangeOrder> findPersonalCurrent(Long uid, String symbol, ExchangeOrderType type, String startTime, String endTime, ExchangeOrderDirection direction, int pageNo, int pageSize) {
        Sort orders = Sort.by(new Sort.Order(Sort.Direction.DESC, "time"));
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, orders);
        Criteria<ExchangeOrder> specification = new Criteria<ExchangeOrder>();
        if(StringUtils.isNotEmpty(symbol)){
            specification.add(Restrictions.eq("symbol", symbol, true));
        }
        if(type!=null&&StringUtils.isNotEmpty(type.toString())){
            specification.add(Restrictions.eq("type", type, true));
        }
        specification.add(Restrictions.eq("memberId", uid, false));
        if (StringUtils.isNotEmpty(startTime)&&StringUtils.isNotEmpty(endTime) ) {
            specification.add(Restrictions.gte("time", Long.valueOf(startTime), true));
            specification.add(Restrictions.lte("time", Long.valueOf(endTime), true));
        }
        if(direction!=null&&StringUtils.isNotEmpty(direction.toString())){
            specification.add(Restrictions.eq("direction", direction, true));
        }
        List<ExchangeOrderStatus> list = new ArrayList<>();
        list.add(ExchangeOrderStatus.TRADING);
        list.add(ExchangeOrderStatus.WAITING_TRIGGER);
        specification.add(Restrictions.in("status",list, false));
        specification.add(Restrictions.eq("marginTrade",BooleanEnum.IS_FALSE,true));
        return exchangeOrderRepository.findAll(specification, pageRequest);
    }

    public int pushWaitingOrderByOrderId(String orderId) {
        return exchangeOrderRepository.pushWaitingOrderByOrderId(orderId);
    }
}
