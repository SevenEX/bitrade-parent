package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.annotation.SecurityVerification;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.util.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.ztuo.bitrade.constant.BooleanEnum.IS_FALSE;
import static cn.ztuo.bitrade.constant.BooleanEnum.IS_TRUE;
import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;
import static cn.ztuo.bitrade.util.BigDecimalUtils.compare;
import static cn.ztuo.bitrade.util.BigDecimalUtils.sub;
import static org.springframework.util.Assert.*;

/**
 * @author Seven
 * @date 2019年01月26日
 */
@RestController
@Slf4j
@Api(tags = "提币")
@RequestMapping(value = "/withdraw", method = RequestMethod.POST)
public class WithdrawController extends BaseController {
    @Autowired
    private MemberAddressService memberAddressService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private WithdrawRecordService withdrawApplyService;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Autowired
    private MemberGradeService gradeService;
    @Autowired
    private CoinExchangeFactory coinExchangeFactory;
    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private  DataDictionaryService dictionaryService;
    /**
     * 增加提现地址
     *
     * @param address
     * @param unit
     * @param remark
     * @param user
     * @return
     */
    @RequestMapping("address/add")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(value = SysConstant.TOKEN_ADD_ADDRESS)
    @ApiOperation("增加提现地址")
    public MessageResult addAddress(String address, String unit, String remark, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        hasText(address, sourceService.getMessage("MISSING_COIN_ADDRESS"));
        hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
        Coin coin = coinService.findByUnit(unit);
        List<MemberAddress> memberAddress = memberAddressService.findByMemberIdAndCoinAndAddress(user.getId(), coin, address, CommonStatus.NORMAL);
        if (memberAddress != null && memberAddress.size() > 0) {
            return error(sourceService.getMessage("ADDRESS_EXITS_ERROR"));
        }
        MessageResult result = memberAddressService.addMemberAddress(user.getId(), address, unit, remark);
        if (result.getCode() == 0) {
            result.setMessage(sourceService.getMessage("ADD_ADDRESS_SUCCESS"));
        } else if (result.getCode() == 500) {
            result.setMessage(sourceService.getMessage("ADD_ADDRESS_FAILED"));
        } else if (result.getCode() == 600) {
            result.setMessage(sourceService.getMessage("COIN_NOT_SUPPORT"));
        }
        return result;
    }

    /**
     * 删除提现地址
     *
     * @param id
     * @param user
     * @return
     */
    @RequestMapping("address/delete")
    @ApiOperation("删除提现地址")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult deleteAddress(long id, @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        MessageResult result = memberAddressService.deleteMemberAddress(user.getId(), id);
        if (result.getCode() == 0) {
            result.setMessage(sourceService.getMessage("DELETE_ADDRESS_SUCCESS"));
        } else {
            result.setMessage(sourceService.getMessage("DELETE_ADDRESS_FAILED"));
        }
        return result;
    }

    /**
     * 提现地址分页信息
     *
     * @param user
     * @param pageNo
     * @param pageSize
     * @param unit
     * @return
     */
    @RequestMapping("address/page")
    @ApiOperation("提现地址分页信息")
    @MultiDataSource(name = "second")
    public MessageResult addressPage(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, int pageNo, int pageSize, String unit) {
        Page<MemberAddress> page = memberAddressService.pageQuery(pageNo, pageSize, user.getId(), unit);
        Page<ScanMemberAddress> scanMemberAddresses = page.map(x -> ScanMemberAddress.toScanMemberAddress(x));
        MessageResult result = MessageResult.success();
        result.setData(scanMemberAddresses);
        return result;
    }

    /**
     * 支持提现的地址
     *
     * @return
     */
    @RequestMapping(value = "support/coin", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation("支持提现的地址")
    @MultiDataSource(name = "second")
    public MessageResult queryWithdraw() {
        List<Coin> list = coinService.findAllCanWithDraw();
        List<String> list1 = new ArrayList<>();
        list.stream().forEach(x -> list1.add(x.getUnit()));
        MessageResult result = MessageResult.success();
        result.setData(list1);
        return result;
    }

    /**
     * 查询所有币种
     *
     * @return
     */
    @RequestMapping(value = "all_coin", method = RequestMethod.GET)
    @ApiOperation("查询所有币种")
    @MultiDataSource(name = "second")
    public MessageResult queryAllCoin() {
        List<Coin> list = coinService.findAllByStatus(CommonStatus.NORMAL);
        List<String> list1 = new ArrayList<>();
        list.stream().forEach(x -> list1.add(x.getUnit()));
        MessageResult result = MessageResult.success();
        result.setData(list1);
        return result;
    }

    /**
     * 提现币种详细信息
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "support/coin/info", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation("提现币种详细信息")
    @MultiDataSource(name = "second")
    public MessageResult queryWithdrawCoin(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user) {
        List<Coin> list = coinService.findAllCanWithDraw();
        List<MemberWallet> list1 = memberWalletService.findAllByMemberId(user.getId());
        long id = user.getId();
        List<WithdrawWalletInfo> list2 = list1.stream().filter(x -> list.contains(x.getCoin())).map(x ->
                WithdrawWalletInfo.builder()
                        .balance(x.getBalance())
                        .withdrawScale(x.getCoin().getWithdrawScale())
                        //.maxTxFee(x.getCoin().getMaxTxFee())
                        .minTxFee(x.getCoin().getMinTxFee())
                        .minAmount(x.getCoin().getMinWithdrawAmount())
                        .maxAmount(x.getCoin().getMaxWithdrawAmount())
                        .name(x.getCoin().getName())
                        .nameCn(x.getCoin().getNameCn())
                        .threshold(x.getCoin().getWithdrawThreshold())
                        .unit(x.getCoin().getUnit())
                        .canAutoWithdraw(x.getCoin().getCanAutoWithdraw())
                        .addresses(memberAddressService.queryAddress(id, x.getCoin().getName())).build()
        ).collect(Collectors.toList());
        MessageResult result = MessageResult.success();
        result.setData(list2);
        return result;
    }

    /**
     * 申请提币
     *
     * @param user
     * @param unit
     * @param coinKey
     * @param address
     * @param amount
     * @param remark
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "apply", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation("申请提币")
    @Transactional(rollbackFor = Exception.class)
    @SecurityVerification(SysConstant.TOKEN_WITHDRAW_AUTH)
    public MessageResult withdraw(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String unit, String coinKey, String address,
                                  BigDecimal amount,  String remark) throws Exception {
        DataDictionary seFeeScaleDict = dictionaryService.findByBond(SysConstant.CAN_WITHDRAW);
        if(seFeeScaleDict.getValue().equals("0")){
            return error(sourceService.getMessage("WITHDRAW_SCALE_ERROR"));
        }
        if(redisUtil.get(SysConstant.WITHDRAW_LOCK+user.getId())!=null&&(Boolean)redisUtil.get(SysConstant.WITHDRAW_LOCK+user.getId())){ return error(sourceService.getMessage("WITHCRAW_LOCK"));}
        hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
        Coin coin = coinService.findByUnit(unit);
        notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));
        if (StringUtils.isNotEmpty(coinKey)) {
            CoinChainRelation relation = coinService.findRelationByKey(coinKey);
            notNull(relation, sourceService.getMessage("COIN_ILLEGAL"));
        } else {
            coinKey = unit;
        }
        if(amount.scale()>coin.getWithdrawScale()){
            return error(sourceService.getMessage("WITHDRAW_SCALE_ERROR"));
        }
        amount = amount.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN);
        BigDecimal fee = coin.getMinTxFee();
        isTrue(coin.getStatus().equals(CommonStatus.NORMAL) && coin.getCanWithdraw().equals(BooleanEnum.IS_TRUE), sourceService.getMessage("COIN_NOT_SUPPORT"));
        //isTrue(compare(fee, new BigDecimal(String.valueOf(coin.getMinTxFee()))), sourceService.getMessage("CHARGE_MIN") + coin.getMinTxFee());
        //isTrue(compare(new BigDecimal(String.valueOf(coin.getMaxTxFee())), fee), sourceService.getMessage("CHARGE_MAX") + coin.getMaxTxFee());
        isTrue(compare(coin.getMaxWithdrawAmount(), amount), sourceService.getMessage("WITHDRAW_MAX") + coin.getMaxWithdrawAmount().stripTrailingZeros().toPlainString());
        isTrue(compare(amount, coin.getMinWithdrawAmount()), sourceService.getMessage("WITHDRAW_MIN") + coin.getMinWithdrawAmount().stripTrailingZeros().toPlainString());
        if (sub(amount, fee).compareTo(BigDecimal.ZERO) < 0) {
            return error(sourceService.getMessage("WITHDRAW_ERROR"));
        }
        memberWalletService.findWalletForUpdate(user.getId(), coin);
        MemberWallet memberWallet = memberWalletService.findByCoinAndMemberId(coin, user.getId());
        isTrue(compare(memberWallet.getBalance(), amount), sourceService.getMessage("INSUFFICIENT_BALANCE"));
//        isTrue(memberAddressService.findByMemberIdAndAddress(user.getId(), address).size() > 0, sourceService.getMessage("WRONG_ADDRESS"));
        isTrue(memberWallet.getIsLock() == BooleanEnum.IS_FALSE, sourceService.getMessage("WALLET_LOCKED"));
        Member member = memberService.findOne(user.getId());
        // 判断用户是否禁止提现
        isTrue(IS_TRUE.equals(member.getWithdrawalStatus()), sourceService.getMessage("WITHDRAW_FORBIDDEN"));
        //是否完成kyc二级认证
        isTrue(member.getKycStatus() == 4, sourceService.getMessage("VIDEO_CHECK"));
        isTrue(member.getMemberLevel() != MemberLevelEnum.GENERAL, sourceService.getMessage("NO_REAL_NAME"));
        MessageResult result = memberWalletService.freezeBalance(memberWallet, amount);
        if (result.getCode() != 0) {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
        //判断该用户当日提币笔数与当日提币数量
        MemberGrade grade = gradeService.findOne(member.getMemberGradeId());
        Object count = redisUtil.get(SysConstant.CUSTOMER_DAY_WITHDRAW_TOTAL_COUNT + user.getId());
        Long countLong = count == null ? 0 : Long.parseLong(count.toString());
        if (countLong > grade.getDayWithdrawCount()) {
            return error(sourceService.getMessage("DAY_WITHDRAW_COUNT_ERROR"));
        }
        Object coverUsdAmount = redisUtil.get(SysConstant.CUSTOMER_DAY_WITHDRAW_COVER_USD_AMOUNT);
        BigDecimal coverUsdAmountBigDecimal = coverUsdAmount == null ? BigDecimal.ZERO : (BigDecimal) coverUsdAmount;
        if (coverUsdAmountBigDecimal.compareTo(grade.getWithdrawCoinAmount()) == 1) {
            return error(sourceService.getMessage("DAY_WITHDRAW_AMOUNT_ERROR"));
        }
        Long expireTime = DateUtil.calculateCurrentTime2SecondDaySec();
        //设置提币笔数 与折合数量
        CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(unit);
        coverUsdAmountBigDecimal = coverUsdAmountBigDecimal.add((rate == null ? BigDecimal.ZERO : rate.getUsdRate()).multiply(amount));
        log.info("该用户提币次数={},提币折合USD数量={}", coverUsdAmountBigDecimal);
        //判断用户等级最大提币数
        isTrue(compare(grade.getWithdrawCoinAmount(), coverUsdAmountBigDecimal), sourceService.getMessage("DAY_WITHDRAW_AMOUNT_ERROR"));
        countLong++;
        redisUtil.set(SysConstant.CUSTOMER_DAY_WITHDRAW_TOTAL_COUNT + user.getId(), countLong, expireTime);
        redisUtil.set(SysConstant.CUSTOMER_DAY_WITHDRAW_COVER_USD_AMOUNT + user.getId(), coverUsdAmountBigDecimal, expireTime);

        WithdrawRecord withdrawApply = new WithdrawRecord();
        withdrawApply.setCoin(coin);
        withdrawApply.setFee(fee);
        withdrawApply.setArrivedAmount(sub(amount, fee));
        withdrawApply.setMemberId(user.getId());
        withdrawApply.setTotalAmount(amount);
        withdrawApply.setAddress(address);
        withdrawApply.setRemark(remark);
        withdrawApply.setCanAutoWithdraw(coin.getCanAutoWithdraw());
        withdrawApply.setCoinKey(coinKey);

        Long targetMemberId = null;
        if (member.getIsQuick().isIs()) {
            if (StringUtils.isNotEmpty(coin.getMasterAddress())) {
                if (StringUtils.equals(coin.getMasterAddress(), address)) {
                    MemberWallet targetWallet = memberWalletService.findByCoinAndAddress(coin, remark);
                    if (targetWallet != null) {
                        targetMemberId = targetWallet.getMemberId();
                    }
                }
            } else {
                MemberWallet targetWallet = null;
                if (StringUtils.isEmpty(coinKey)) {
                    targetWallet = memberWalletService.findByCoinAndAddress(coin, address);
                    if (targetWallet != null) {
                        targetMemberId = targetWallet.getMemberId();
                    }
                } else {
                    MemberWalletRelation relation = memberWalletService.findWalletRelationByCoinKeyAndAddress(coinKey, address);
                    if(relation != null){
                        targetMemberId = relation.getMemberId();
                    }

                }
            }
        }
        if (targetMemberId != null) {
            withdrawApply.setIsQuick(BooleanEnum.IS_TRUE);
        } else {
            withdrawApply.setIsQuick(BooleanEnum.IS_FALSE);
        }

        if (withdrawApply.getIsQuick().isIs()) {
            result = memberWalletService.decreaseFrozen(memberWallet.getId(), amount);
            if (result.getCode() != 0) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(amount.negate());
            transaction.setSymbol(coin.getUnit());
            transaction.setAddress(memberWallet.getAddress());
            transaction.setMemberId(member.getId());
            transaction.setType(TransactionType.WITHDRAW);
            transaction.setFee(fee.negate());
            transaction.setTxid(null);
            transaction.setIsQuick(IS_TRUE);
            transactionService.save(transaction);
            memberWalletService.recharge(coin, targetMemberId, address, sub(amount, fee), null, BooleanEnum.IS_TRUE);
            withdrawApply.setStatus(WithdrawStatus.SUCCESS);
            withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
            withdrawApply.setDealTime(withdrawApply.getCreateTime());
            withdrawApplyService.save(withdrawApply);
            return MessageResult.success(sourceService.getMessage("APPLY_QUICK_SUCCESS"));
        }
        //提币数量低于或等于阈值并且自动转账打开 直接自动放币
        else if (amount.compareTo(coin.getWithdrawThreshold()) <= 0 && coin.getCanAutoWithdraw().equals(BooleanEnum.IS_TRUE)) {
            withdrawApply.setStatus(WithdrawStatus.WAITINGTRANSFER);
            withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
            withdrawApply.setDealTime(withdrawApply.getCreateTime());
            WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);
            JSONObject json = new JSONObject();
            json.put("uid", user.getId());
            //提币总数量
            json.put("totalAmount", amount);
            //手续费
            json.put("fee", fee);
            //预计到账数量
            json.put("arriveAmount", sub(amount, fee));
            //币种
            json.put("coinUnit", coin.getUnit());
            //提币地址
            json.put("address", address);
            //提币记录id
            json.put("withdrawId", withdrawRecord.getId());
            json.put("remark", remark);
            kafkaTemplate.send("withdraw", StringUtils.isNotEmpty(coinKey) ? coinKey : coin.getUnit(), json.toJSONString());
            return MessageResult.success(sourceService.getMessage("APPLY_SUCCESS"));
            // 自动打币关闭进入人工打币
        }else if(!BooleanEnum.IS_TRUE.equals(coin.getCanAutoWithdraw())) {
            withdrawApply.setStatus(WithdrawStatus.PROCESSING);
            withdrawApply.setIsAuto(IS_FALSE);
            if (withdrawApplyService.save(withdrawApply) != null) {
                return MessageResult.success(sourceService.getMessage("APPLY_AUDIT"));
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
            // 超过阈值，进入人工审核。审核通过后自动打币
        } else {
            withdrawApply.setStatus(WithdrawStatus.PROCESSING);
            withdrawApply.setIsAuto(IS_TRUE);
            if (withdrawApplyService.save(withdrawApply) != null) {
                return MessageResult.success(sourceService.getMessage("APPLY_AUDIT"));
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }
//        //提币数量低于或等于阈值并且该币种支持自动提币
//        if (amount.compareTo(coin.getWithdrawThreshold()) <= 0 && coin.getCanAutoWithdraw().equals(BooleanEnum.IS_TRUE)) {
//            Double withAmountSum=sumDailyWithdraw(coin);
//            //如果币种设置了单日最大提币量，并且当天已申请的数量（包括待审核、待放币、成功、转账中状态的所有记录）加上当前提币量大于每日最大提币量
//            // 进入人工审核
//            if(coin.getMaxDailyWithdrawRate()!=null&&coin.getMaxDailyWithdrawRate().compareTo(BigDecimal.ZERO)>0
//                    &&coin.getMaxDailyWithdrawRate().compareTo(new BigDecimal(withAmountSum).add(amount))<0){
//                withdrawApply.setStatus(WithdrawStatus.PROCESSING);
//                withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
//                if (withdrawApplyService.save(withdrawApply) != null) {
//                    return MessageResult.success(sourceService.getMessage("APPLY_AUDIT"));
//                } else {
//                    throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
//                }
//            }else if(withdrawApply.getIsQuick().isIs()){
//                memberWallet.setFrozenBalance(memberWallet.getFrozenBalance().subtract(amount));
//                MemberTransaction transaction = new MemberTransaction();
//                transaction.setAmount(amount.negate());
//                transaction.setSymbol(coin.getUnit());
//                transaction.setAddress(memberWallet.getAddress());
//                transaction.setMemberId(member.getId());
//                transaction.setType(TransactionType.WITHDRAW);
//                transaction.setFee(fee.negate());
//                transaction.setTxid(null);
//                transaction.setIsQuick(IS_TRUE);
//                transactionService.save(transaction);
//                memberWalletService.recharge(coin, targetMemberId, address,sub(amount, fee),null,BooleanEnum.IS_TRUE);
//                withdrawApply.setStatus(WithdrawStatus.SUCCESS);
//                withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
//                withdrawApply.setDealTime(withdrawApply.getCreateTime());
//                withdrawApplyService.save(withdrawApply);
//                return MessageResult.success(sourceService.getMessage("APPLY_QUICK_SUCCESS"));
//            }else{
//                withdrawApply.setStatus(WithdrawStatus.WAITING);
//                withdrawApply.setIsAuto(BooleanEnum.IS_TRUE);
//                withdrawApply.setDealTime(withdrawApply.getCreateTime());
//                WithdrawRecord withdrawRecord = withdrawApplyService.save(withdrawApply);
//                JSONObject json = new JSONObject();
//                json.put("uid", user.getId());
//                //提币总数量
//                json.put("totalAmount", amount);
//                //手续费
//                json.put("fee", fee);
//                //预计到账数量
//                json.put("arriveAmount", sub(amount, fee));
//                //币种
//                json.put("coinUnit", coin.getUnit());
//                //提币地址
//                json.put("address", address);
//                //提币记录id
//                json.put("withdrawId", withdrawRecord.getId());
//                json.put("remark",remark);
//                kafkaTemplate.send("withdraw", StringUtils.isNotEmpty(coinKey) ? coinKey : coin.getUnit(), json.toJSONString());
//                return MessageResult.success(sourceService.getMessage("APPLY_SUCCESS"));
//            }
//        } else {
//            withdrawApply.setStatus(WithdrawStatus.PROCESSING);
//            withdrawApply.setIsAuto(BooleanEnum.IS_FALSE);
//            if (withdrawApplyService.save(withdrawApply) != null) {
//                return MessageResult.success(sourceService.getMessage("APPLY_AUDIT"));
//            } else {
//                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
//            }
//        }
    }


    /**
     * 提币记录
     *
     * @param user
     * @return
     */
    @GetMapping("record")
    @ApiOperation("提币记录")
    @MultiDataSource(name = "second")
    public MessageResult pageWithdraw(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, PageModel pageModel,
                                      String unit) {
        MessageResult mr = new MessageResult(0, messageSource.getMessage("SUCCESS"));
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(unit)) {
            booleanExpressions.add(QWithdrawRecord.withdrawRecord.coin.unit.eq(unit));
        }
        booleanExpressions.add(QWithdrawRecord.withdrawRecord.memberId.eq(user.getId()));
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<WithdrawRecord> records = withdrawApplyService.findAll(predicate, pageModel);
        records.map(x -> ScanWithdrawRecord.toScanWithdrawRecord(x));
        mr.setData(records);
        return mr;
    }

    /**
     * 当日已申请数量
     *
     * @return
     */
    @GetMapping("todayWithdrawSum")
    @ApiOperation("当日已申请数量")
    @MultiDataSource(name = "second")
    public MessageResult todayWithdrawSum(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember user, String symbol) {
        if (StringUtils.isEmpty(symbol)) {
            return error("symbol is not null");
        }
        Coin coin = coinService.findByUnit(symbol);
        if (coin == null) {
            return error("coin has not found");
        }
        Double withAmountSum = sumDailyWithdraw(coin);
        MessageResult result = MessageResult.success();
        result.setData(withAmountSum);
        return result;
    }

    private Double sumDailyWithdraw(Coin coin) {
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -1);
        Date startTime = calendar.getTime();
        Double withAmountSum = withdrawApplyService.countWithdrawAmountByTimeAndMemberIdAndCoin(startTime, endTime, coin);
        if (withAmountSum == null) {
            withAmountSum = 0.0;
        }
        return withAmountSum;
    }

}
