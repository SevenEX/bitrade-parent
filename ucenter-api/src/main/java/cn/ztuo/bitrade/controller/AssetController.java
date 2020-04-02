package cn.ztuo.bitrade.controller;


import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.core.Convert;
import cn.ztuo.bitrade.dao.MemberWalletSeHistoryDao;
import cn.ztuo.bitrade.dao.MemberWalletRelationDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.transform.AuthMember;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.ztuo.bitrade.constant.SysConstant.SESSION_MEMBER;

@RestController
@RequestMapping("/asset")
@Slf4j
@Api(tags = "用户资金管理")
public class AssetController extends BaseController{
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberWalletSeHistoryDao walletSeHistoryDao;
    @Autowired
    private MemberWalletRelationDao memberWalletRelationDao;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private CoinExchangeFactory coinExchangeFactory;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private LockPositionRecordService lockPositionRecordService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private OtcCoinService otcCoinService;
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Autowired
    private OtcWalletService otcWalletService;
    @Autowired
    private LocalizationExtendService localizationExtendService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping(value = "wallet",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "用户钱包信息")
    public MessageResult findWallet(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,String name) {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        List<MemberWallet> wallets = walletService.findAllByMemberIdAndCoin(name==null?"":name,member.getId());
        List<MemberWalletRelation> walletRelations;
        if(StringUtils.isNotEmpty(name)){
            walletRelations = memberWalletRelationDao.findAllByMemberIdAndCoinId(member.getId(), name);
        }
        else {
            walletRelations = memberWalletRelationDao.findAllByMemberId(member.getId());
        }
        ImmutableListMultimap<String, MemberWalletRelation> walletRelationsMap = Multimaps.index(walletRelations, MemberWalletRelation::getCoinId);
        List otcUnits = otcCoinService.findAllUnitsByStatus();
        wallets = wallets.stream().filter(wallet -> wallet.getCoin().getStatus() == CommonStatus.NORMAL).collect(Collectors.toList());
        wallets.forEach(wallet -> {
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getCoin().getUnit());
            // 过滤已禁用币种
             if (rate != null) {
//            if (rate != null && wallet.getCoin().getStatus() == CommonStatus.NORMAL) {
                wallet.getCoin().setUsdRate(rate.getUsdRate());
                wallet.getCoin().setCnyRate(rate.getCnyRate());
                wallet.getCoin().setSgdRate(rate.getSgdRate());
                String cnName = localizationExtendService.getLocaleInfo("Coin", locale, wallet.getCoin().getName(), "name");
                wallet.getCoin().setNameCn(StringUtils.firstNonBlank(cnName, wallet.getCoin().getName()));
                wallet.setExchangeCoinList(exchangeCoinService.getExchangeSymbol(wallet.getCoin().getUnit()));
                wallet.setAddressList(walletRelationsMap.get(wallet.getCoin().getName()));
                if(otcUnits.contains(wallet.getCoin().getUnit())){
                    wallet.setCanOtc(true);
                }
            } else {
                log.info("unit = {} , rate = null ", wallet.getCoin().getUnit());
            }
        });
        MessageResult mr = MessageResult.success(messageSource.getMessage("SUCCESS"));
        mr.setData(wallets);
        return mr;
    }

    /**
     * 用户钱包信息
     *
     * @param member
     * @return
     */
    @RequestMapping(value = "total",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "总资产折合")
    public MessageResult total(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member) {
        Map map = new HashMap();
        BigDecimal walletTotal = BigDecimal.ZERO;
        BigDecimal otcTotal = BigDecimal.ZERO;
        List<MemberWallet> wallets = walletService.findAllByMemberId(member.getId());
        List<OtcWallet> results = otcWalletService.findByMemberId(member.getId());
        for(MemberWallet wallet : wallets){
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(wallet.getCoin().getUnit());
            if(wallet.getBalance().compareTo(BigDecimal.ZERO) > 0 && rate.getUsdRate() != null) {
                walletTotal = walletTotal.add(wallet.getBalance().multiply(rate.getUsdRate()));
            }
        }
        for(OtcWallet result : results){
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(result.getCoin().getUnit());
            if(result.getBalance().compareTo(BigDecimal.ZERO) > 0 && rate.getUsdRate() != null) {
                otcTotal = otcTotal.add(result.getBalance().multiply(rate.getUsdRate()));
            }
        }
        map.put("walletTotal",walletTotal);
        map.put("otcTotal",otcTotal);
        map.put("total",walletTotal.add(otcTotal));
        map.put("rate",coinExchangeFactory.get("USDT"));
        MessageResult mr = MessageResult.success(messageSource.getMessage("SUCCESS"));
        mr.setData(map);
        return mr;
    }

    /**
     * 查询特定类型的记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @param type
     * @return
     */
    @RequestMapping(value = "transaction",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "查询特定类型的记录")
    @MultiDataSource(name = "second")
    public MessageResult findTransaction(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, int pageNo, int pageSize, String type,String symbol,
                                        String startTime, String endTime) throws Exception{
        List<TransactionType> typeList = null;
        if(StringUtils.isNotEmpty(type)) {
            typeList = Arrays.stream(type.split(",")).map(item -> TransactionType.valueOfOrdinal(Integer.parseInt(item))).collect(Collectors.toList());
        }
        Page page = transactionService.queryByMember(member.getId(), pageNo, pageSize, typeList ,startTime,endTime,symbol);
        return success(page);
    }

    /**
     * 查询所有记录
     *
     * @param member
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "transaction/all",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "查询所有记录")
    @MultiDataSource(name = "second")
    public MessageResult findTransaction(
            @ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,
            HttpServletRequest request,
            int pageNo,
            int pageSize,
            @RequestParam(value = "symbol",required = false) String symbol) throws ParseException {
        TransactionType type = null;
        if (StringUtils.isNotEmpty(request.getParameter("type"))) {
            type = TransactionType.valueOfOrdinal(Convert.strToInt(request.getParameter("type"), 0));
        }
        String startDate = "";
        String endDate = "";
        if (StringUtils.isNotEmpty(request.getParameter("dateRange"))) {
            String[] parts = request.getParameter("dateRange").split("~");
            startDate = parts[0].trim();
            endDate = parts[1].trim();
        }
        return success(transactionService.queryByMember(member.getId(), pageNo, pageSize, type, startDate, endDate,symbol));
    }

    @RequestMapping(value = "wallet/{symbol}",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "查询钱包地址和余额")
    @MultiDataSource(name = "second")
    public MessageResult findWalletBySymbol(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, @PathVariable String symbol) {
        MessageResult mr = MessageResult.success(messageSource.getMessage("SUCCESS"));
        MemberWallet memberWallet = walletService.findByCoinUnitAndMemberId(symbol, member.getId());
        List<MemberWalletRelation> walletRelations = memberWalletRelationDao.findAllByMemberIdAndCoinId(member.getId(), symbol);
        ImmutableListMultimap<String, MemberWalletRelation> walletRelationsMap = Multimaps.index(walletRelations, MemberWalletRelation::getCoinId);
        memberWallet.setAddressList(walletRelationsMap.get(memberWallet.getCoin().getName()));
        mr.setData(memberWallet);
        return mr;
    }

    @RequestMapping(value = "wallet/reset-address",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "重置地址")
    public MessageResult resetWalletAddress(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member, String unit) {
        try {
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            kafkaTemplate.send("reset-member-address", unit, json.toJSONString());
            return MessageResult.success(messageSource.getMessage("SUCCESS"));
        } catch (Exception e) {
            return MessageResult.error(messageSource.getMessage("UNKNOWN.ERROR"));
        }
    }

    @PostMapping("lock-position")
    @ApiOperation(value = "锁仓记录")
    public MessageResult lockPositionRecordList(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,
                                                @RequestParam(value = "status",required = false) CommonStatus status,
                                                @RequestParam(value = "coinUnit",required = false)String coinUnit,
                                                PageModel pageModel){
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(status!=null){
            booleanExpressions.add(QLockPositionRecord.lockPositionRecord.status.eq(status));
        }
        if(coinUnit!=null){
            Coin coin=coinService.findByUnit(coinUnit);
            if(coin==null){
                return MessageResult.error(sourceService.getMessage("COIN_ILLEGAL"));
            }
            booleanExpressions.add(QLockPositionRecord.lockPositionRecord.coin.eq(coin));
        }
        booleanExpressions.add(QLockPositionRecord.lockPositionRecord.memberId.eq(member.getId()));
        Predicate predicate=PredicateUtils.getPredicate(booleanExpressions);
        Page<LockPositionRecord> lockPositionRecordList=lockPositionRecordService.findAll(predicate,pageModel);
        MessageResult result=MessageResult.success();
        result.setData(lockPositionRecordList);
        return result;
    }

    @PostMapping("se-history")
    @ApiOperation(value = "SE持仓记录")
    public MessageResult seHistory(@ApiIgnore @SessionAttribute(SESSION_MEMBER) AuthMember member,
                                   PageModel pageModel){
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        booleanExpressions.add(QMemberWalletSeHistory.memberWalletSeHistory.memberId.eq(member.getId()));
        Predicate predicate=PredicateUtils.getPredicate(booleanExpressions);
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "createTime"));
        Pageable pageable = PageRequest.of(pageModel.getPageNo() - 1, pageModel.getPageSize(), sort);
        Page<MemberWalletSeHistory> memberWalletSeHistoryList = walletSeHistoryDao.findAll(predicate,pageable);
        MessageResult result=MessageResult.success();
        result.setData(memberWalletSeHistoryList);
        return result;
    }


}
