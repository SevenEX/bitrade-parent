package cn.ztuo.bitrade.controller.margin;

import cn.ztuo.bitrade.annotation.MultiDataSource;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.service.*;
import cn.ztuo.bitrade.system.CoinExchangeFactory;
import cn.ztuo.bitrade.util.FileUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.Assert.notNull;

/**
 * @author MrGao
 * @description 杠杆交易手续费
 * @date 2018/1/19 15:16
 */
@Slf4j
@RestController
@RequestMapping("lever/lever_coin")
@Api(tags = "杠杆交易（暂弃）")
public class LeverCoinController extends BaseAdminController {

    @Value("${bdtop.system.md5.key}")
    private String md5Key;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private CoinService coinService;

    @Autowired
    private CoinExchangeFactory coinExchangeFactory;

    @Autowired
    private LeverWalletService leverWalletService;

    @Autowired
    private LeverCoinService leverCoinService;

    @Autowired
    private LoanRecordService loanRecordService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private LeverWalletTransferRecordService leverWalletTransferRecordService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ExchangeCoinService exchangeCoinService;

    /**
     * 查询所有币种
     *
     * @return
     */
    @RequestMapping(value = "all_coin", method = RequestMethod.GET)
    @AccessLog(module = AdminModule.EXCHANGE, operation = "查询所有币种")
    @ApiOperation(value = "查询所有币种")
    @MultiDataSource(name = "second")
    public MessageResult listAllCoin() {
        MessageResult result = MessageResult.success();
        List<Coin> coinList = coinService.findByStatus(CommonStatus.NORMAL);
        result.setData(coinList);
        return result;
    }

    /**
     * 新增杠杆交易对
     *
     * @param leverCoin
     * @return
     */
    @RequiresPermissions("lever:lever-coin:merge")
    @RequestMapping(value = "merge", method = RequestMethod.POST)
    @AccessLog(module = AdminModule.EXCHANGE, operation = "添加杠杆币对")
    @ApiOperation(value = "添加杠杆币对")
    public MessageResult exchangeCoinList(
            @Valid LeverCoin leverCoin) {
        if (leverCoin.getCoinSymbol().equalsIgnoreCase(leverCoin.getBaseSymbol())) {
            return MessageResult.error(msService.getMessage("MUST_DIFFERENT_COIN"));
        }
        ExchangeCoin oldExchangeCoin = exchangeCoinService.findBySymbol(leverCoin.getSymbol());
        if (oldExchangeCoin == null) {
            return MessageResult.error(msService.getMessage("NEED_EXCHANGE_COIN"));
        }
        if (leverCoin.getProportion().compareTo(BigDecimal.ONE) <= 0) {
            return MessageResult.error("validate proportion");
        }
        leverCoin = leverCoinService.save(leverCoin);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), leverCoin);
    }

    /**
     * 分页查询杠杆交易对
     *
     * @param pageModel
     * @return
     */
    @RequiresPermissions("lever:lever-coin:page-query")
    @RequestMapping(value = "page_query", method = RequestMethod.POST)
    //@AccessLog(module = AdminModule.MARGIN, operation = "分页查找杠杆币对")
    @ApiOperation(value = "分页查找杠杆币对")
    @MultiDataSource(name = "second")
    public MessageResult exchangeCoinList(PageModel pageModel) {
        if (pageModel.getProperty() == null) {
            List<String> list = new ArrayList<>();
            List<Sort.Direction> directions = new ArrayList<>();
            list.add("symbol");
            directions.add(Sort.Direction.DESC);
            pageModel.setProperty(list);
            pageModel.setDirection(directions);
        }
        Page<LeverCoin> all = leverCoinService.findAll(new BooleanBuilder(), pageModel.getPageable());
        return success(all);
    }

    /**
     * 查询所有杠杆交易对
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "find_all", method = RequestMethod.GET)
   // @AccessLog(module = AdminModule.MARGIN, operation = "导出杠杆币对 Excel")
    @ApiOperation(value = "导出杠杆币对")
    @MultiDataSource(name = "second")
    public MessageResult findAll() throws Exception {
        List all = leverCoinService.findAll();
        return success(all);
    }

    /**
     * 杠杆币对详情
     *
     * @param symbol
     * @return
     */
    @RequiresPermissions("lever:lever-coin:page-query")
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    //@AccessLog(module = AdminModule.MARGIN, operation = "杠杆币对详情")
    @ApiOperation(value = "杠杆币对详情")
    @MultiDataSource(name = "second")
    public MessageResult detail(
            @RequestParam(value = "symbol") String symbol) {
        LeverCoin exchangeCoin = leverCoinService.getBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        return success(exchangeCoin);
    }


    /**
     * 杠杆币对删除
     *
     * @param ids
     * @return
     */
    @RequiresPermissions("lever:lever-coin:deletes")
    @RequestMapping(value = "deletes", method = RequestMethod.POST)
    @AccessLog(module = AdminModule.MARGIN, operation = "杠杆币对删除")
    @ApiOperation(value = "杠杆币对删除")
    public MessageResult deletes(
            @RequestParam(value = "ids") Long[] ids) {
        leverCoinService.deletes(ids);
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * 修改杠杆交易对
     *
     * @param symbol
     * @param enable
     * @param sort
     * @param interestRate
     * @param proportion
     * @param minTurnIntoAmount
     * @param minTurnOutAmount
     * @return
     */
    @RequiresPermissions("lever:lever-coin:alter-rate")
    @RequestMapping(value = "alter_rate", method = RequestMethod.POST)
    @AccessLog(module = AdminModule.MARGIN, operation = "修改杠杆交易币对")
    @ApiOperation(value = "修改杠杆交易币对")
    public MessageResult alterExchangeCoinRate(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "enable", required = false) BooleanEnum enable,
            @RequestParam(value = "sort", required = false) Integer sort,
            @RequestParam(value = "interestRate", required = false) BigDecimal interestRate,
            @RequestParam(value = "proportion") BigDecimal proportion,
            @RequestParam(value = "minTurnIntoAmount", defaultValue = "0", required = false) BigDecimal minTurnIntoAmount,
            @RequestParam(value = "minTurnOutAmount", defaultValue = "0", required = false) BigDecimal minTurnOutAmount) {
        LeverCoin exchangeCoin = leverCoinService.getBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if (proportion.compareTo(BigDecimal.ONE) <= 0) {
            return MessageResult.error("validate proportion");
        }
        if (sort != null) {
            //设置排序
            exchangeCoin.setSort(sort);
        }
        if (enable != null) {
            //设置启用 禁用
            exchangeCoin.setEnable(enable);
        }
        if (interestRate != null) {
            exchangeCoin.setInterestRate(interestRate);
        }

        exchangeCoin.setProportion(proportion);

        if (minTurnIntoAmount != null) {
            exchangeCoin.setMinTurnIntoAmount(minTurnIntoAmount);
        }
        if (minTurnOutAmount != null) {
            exchangeCoin.setMinTurnOutAmount(minTurnOutAmount);
        }
        leverCoinService.save(exchangeCoin);
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * 导出杠杆币对
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequiresPermissions("lever:lever-coin:out_excel")
    @RequestMapping(value = "out_excel", method = RequestMethod.GET)
    @AccessLog(module = AdminModule.MARGIN, operation = "导出杠杆币对 Excel")
    @ApiOperation(value = "导出杠杆币对")
    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List all = leverCoinService.findAll();
        return new FileUtil().exportExcel(request, response, all, "exchangeCoin");
    }

    /**
     * 获取所有交易区币种的单位
     *
     * @return
     */
   // @AccessLog(module = AdminModule.MARGIN, operation = "获取所有交易区币种的单位")
    @RequiresPermissions("lever:lever-coin:all_base_symbol_units")
    @RequestMapping(value = "all_base_symbol_units", method = RequestMethod.GET)
    @ApiOperation(value = "获取所有交易区币种的单位")
    @MultiDataSource(name = "second")
    public MessageResult getAllBaseSymbolUnits() {
        List<String> list = leverCoinService.getBaseSymbol();
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * 获取交易区币种 所支持的交易 币种
     *
     * @return
     */
   // @AccessLog(module = AdminModule.MARGIN, operation = "获取交易区币种 所支持的交易 币种")
    @RequiresPermissions("lever:lever-coin:all_coin_symbol_units")
    @RequestMapping(value = "all_coin_symbol_units", method = RequestMethod.POST)
    @ApiOperation(value = "获取交易区币种 所支持的交易 币种")
    @MultiDataSource(name = "second")
    public MessageResult getAllCoinSymbolUnits(@RequestParam("baseSymbol") String baseSymbol) {
        List<String> list = leverCoinService.getCoinSymbol(baseSymbol);
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * 查询杠杆交易钱包
     *
     * @param memberId
     * @return
     */
    @RequiresPermissions("lever:lever-coin:list")
    @RequestMapping(value = "list", method = RequestMethod.POST)
   // @AccessLog(module = AdminModule.MARGIN, operation = "查询杠杆交易钱包")
    @ApiOperation(value = "查询杠杆交易钱包")
    @MultiDataSource(name = "second")
    public MessageResult listLeverWallet(String symbol, Long memberId, Integer pageNum, Integer pageSize) {
        List<LeverCoin> leverCoinList = new ArrayList<>();
        if (symbol == null || symbol.equals("")) {
            leverCoinList = leverCoinService.findByEnable(BooleanEnum.IS_TRUE);
        } else {
            LeverCoin leverCoin = leverCoinService.getBySymbol(symbol);
            if (leverCoin == null) {
                return MessageResult.error(msService.getMessage("SYMBOL_NOT_FOUND"));
            }
            leverCoinList.add(leverCoin);
        }
        MessageResult result = MessageResult.success();
        if (leverCoinList != null && leverCoinList.size() > 0) {
            List<LeverWallet> leverWalletList;
            if (memberId != null && !memberId.equals("")) {
                leverWalletList = leverWalletService.findByMemberId(memberId);
            } else {
                Sort sort = Sort.by(Sort.Direction.DESC, "id");
                PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, sort);
                Page<LeverWallet> page = leverWalletService.findAll(new BooleanBuilder(), pageRequest);
                leverWalletList = page.getContent();
            }
            List<LeverWalletVO> voList = new ArrayList<>();
            Coin btcCoin = coinService.findByUnit(LeverWalletVO.btcUnit);
            CoinExchangeFactory.ExchangeRate btcRate = coinExchangeFactory.get(LeverWalletVO.btcUnit);
            btcCoin.setSgdRate(btcRate.getSgdRate());
            btcCoin.setCnyRate(btcRate.getCnyRate());
            btcCoin.setUsdRate(btcRate.getUsdRate());
            if (leverWalletList != null && leverWalletList.size() > 0) {
                LeverWalletVO vo = new LeverWalletVO();
                List<LeverWallet> list = new ArrayList<>();
                for (LeverWallet leverWallet : leverWalletList) {
                    vo.setRiskRate(getCalculatedRisk(leverWallet.getMemberId(), leverWallet.getLeverCoin().getId()).getRiskRate());
                    vo.setMemberId(leverWallet.getMemberId());
                    vo.setSymbol(leverWallet.getLeverCoin().getSymbol());
                    vo.setProportion(leverWallet.getLeverCoin().getProportion());
                    if(list.size() == 0){
                        list.add(leverWallet);
                    }else if(list.size()!=0){
                        LeverWallet memberWallet = list.get(0);
                        if(leverWallet.getMemberId().equals(memberWallet.getMemberId())){
                            list.add(leverWallet);
                            voList.add(vo);
                            vo.setLeverWalletList(list);
                            checkLoanUpper(vo, btcCoin);
                            calculateExplosionPrice(vo);
                        }else {
                            vo = new LeverWalletVO();
                            list = new ArrayList<>();
                            vo.setRiskRate(getCalculatedRisk(leverWallet.getMemberId(), leverWallet.getLeverCoin().getId()).getRiskRate());
                            vo.setMemberId(leverWallet.getMemberId());
                            vo.setSymbol(leverWallet.getLeverCoin().getSymbol());
                            vo.setProportion(leverWallet.getLeverCoin().getProportion());
                            list.add(leverWallet);
                        }
                    }
                }
            }

            result.setTotal(Long.valueOf(voList.size()));
            result.setData(voList);
        }
        return result;
    }

    /**
     * 计算爆仓价
     *
     * @param vo
     */
    private void calculateExplosionPrice(LeverWalletVO vo) {
        //基准币借贷
        BigDecimal baseLoan = vo.getBaseLoanCount();
        //交易币借贷
        BigDecimal coinLoan = vo.getCoinLoanCount();
        BigDecimal explosionRate = vo.getExplosionRiskRate();
        explosionRate = explosionRate.divide(BigDecimal.valueOf(100));
        String symbol = vo.getSymbol();
        String baseCoin = symbol.split("/")[1];
        String exchangeCoin = symbol.split("/")[0];
        //用户当前账户余额
        BigDecimal baseBalance = BigDecimal.ZERO, coinBalance = BigDecimal.ZERO;
        List<LeverWallet> leverWallets = vo.getLeverWalletList();
        for (LeverWallet leverWallet : leverWallets) {
            if (leverWallet.getCoin().getUnit().equalsIgnoreCase(baseCoin)) {
                baseBalance = leverWallet.getBalance();
            } else if (leverWallet.getCoin().getUnit().equalsIgnoreCase(exchangeCoin)) {
                coinBalance = leverWallet.getBalance();
            }
        }
        //若借的为基准币 爆仓率= （交易币*爆仓价+基准币）/总借币
        if (baseLoan.compareTo(BigDecimal.ZERO) > 0 && coinLoan.compareTo(BigDecimal.ZERO) == 0) {
            if (coinBalance.compareTo(BigDecimal.ZERO) == 0) {
                vo.setExplosionPrice(BigDecimal.ZERO);
            } else {
                vo.setExplosionPrice(baseLoan.multiply(explosionRate).subtract(baseBalance).divide(coinBalance, 6, BigDecimal.ROUND_HALF_DOWN));
            }
        }
        //计算爆仓价格 借的为交易币 爆仓率=（交易币*爆仓价+基准币）/总借币*爆仓价  爆仓价=基准币/((总借币*爆仓率)-交易币)
        if (coinLoan.compareTo(BigDecimal.ZERO) > 0 && baseLoan.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal coefficient = coinLoan.multiply(explosionRate).subtract(coinBalance);
            if (coefficient.compareTo(BigDecimal.ZERO) > 0) {
                vo.setExplosionPrice(baseBalance.divide(coefficient, 6, BigDecimal.ROUND_HALF_DOWN));
            } else {
                vo.setExplosionPrice(BigDecimal.ZERO);
            }
        }
        if (coinLoan.compareTo(BigDecimal.ZERO) > 0 && baseLoan.compareTo(BigDecimal.ZERO) > 0) {

        }
    }

    /**
     * 查询划转记录
     *
     * @param symbol
     * @param coinUnit
     * @param type
     * @param userName
     * @param pageModel
     * @return
     */
    @RequiresPermissions("lever:lever-coin:transfer")
    @RequestMapping(value = "transfer", method = RequestMethod.POST)
   // @AccessLog(module = AdminModule.MARGIN, operation = "查询划转记录")
    @ApiOperation(value = "查询划转记录")
    @MultiDataSource(name = "second")
    public MessageResult listTransferRecord(String symbol, String coinUnit, Integer type, String userName, PageModel pageModel) {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(symbol)) {
            LeverCoin leverCoin = leverCoinService.getBySymbol(symbol);
            if (leverCoin == null) {
                return MessageResult.error("symbol has not found");
            }
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.leverCoin.eq(leverCoin));
        }
        if (StringUtils.isNotBlank(coinUnit)) {
            Coin coin = coinService.findByUnit(coinUnit);
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.coin.eq(coin));
        }
        if (type != null) {
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.type.eq(type));
        }
        if (StringUtils.isNotBlank(userName)) {
            booleanExpressions.add(QLeverWalletTransferRecord.leverWalletTransferRecord.memberName.like(userName));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Page<LeverWalletTransferRecord> page = leverWalletTransferRecordService.findAll(predicate, pageModel);
        MessageResult result = MessageResult.success();
        result.setData(page);
        return result;
    }


    /**
     * 锁定杠杆账户或解锁
     *
     * @param wallet
     * @return
     */
    @RequiresPermissions("lever:lever-coin:lock_wallet")
    @RequestMapping(value = "lock_wallet", method = RequestMethod.POST)
   // @AccessLog(module = AdminModule.MARGIN, operation = "锁定杠杆账户或解锁")
    @ApiOperation(value = "锁定杠杆账户或解锁")
    public MessageResult lockWallet(LeverWallet wallet) {
        LeverWallet dataWallet = leverWalletService.findOne(wallet.getId());
        dataWallet.setIsLock(wallet.getIsLock());
        leverWalletService.save(dataWallet);
        MessageResult result = MessageResult.success();
        result.setData(dataWallet);
        return result;
    }

    /**
     * 查询各个杠杆用户风险率
     *
     * @param pageModel
     * @return
     */
    @RequiresPermissions("lever:lever-coin:risk_list")
    @RequestMapping(value = "risk_list", method = RequestMethod.POST)
   // @AccessLog(module = AdminModule.MARGIN, operation = "查询各个杠杆用户风险率")
    @ApiOperation(value = "查询各个杠杆用户风险率")
    @MultiDataSource(name = "second")
    public MessageResult riskList(PageModel pageModel) {
        Map map = leverWalletService.listMarginMember(pageModel.getPageable());
        List<MarginMemberVO> marginMemberVOList = (List<MarginMemberVO>) map.get("content");
        if (marginMemberVOList != null && marginMemberVOList.size() > 0) {
            for (MarginMemberVO marginMemberVO : marginMemberVOList) {
                InspectBean inspectBean = getCalculatedRisk(marginMemberVO.getMemberId(), marginMemberVO.getLeverCoinId());
                marginMemberVO.setInspectBean(inspectBean);
            }
        }
        MessageResult result = MessageResult.success();
        result.setData(map);
        return result;
    }

    /**
     * 查询借贷上线
     *
     * @param vo
     * @param btcCoin
     */
    public void checkLoanUpper(LeverWalletVO vo, Coin btcCoin) {
        LeverCoin leverCoin = vo.getLeverWalletList().get(0).getLeverCoin();
        List<LoanRecord> loanRecordList = loanRecordService.findByMemberIdAndLeverCoinAndRepayment(vo.getMemberId(), leverCoin, BooleanEnum.IS_FALSE);
        //可借贷总金额，单位美元
        List<LeverWallet> leverWalletList = vo.getLeverWalletList();
        BigDecimal totalCanLoanAmountUsd = BigDecimal.ZERO;//钱包余额
        for (LeverWallet leverWallet : leverWalletList) {
            CoinExchangeFactory.ExchangeRate coinRate = coinExchangeFactory.get(leverWallet.getCoin().getUnit());
            totalCanLoanAmountUsd = totalCanLoanAmountUsd.add(leverWallet.getBalance()
                    .multiply(leverCoin.getProportion().subtract(BigDecimal.ONE)).multiply(coinRate.getUsdRate()));
            leverWallet.getCoin().setUsdRate(coinRate.getUsdRate());
            leverWallet.getCoin().setCnyRate(coinRate.getCnyRate());
            leverWallet.getCoin().setSgdRate(coinRate.getSgdRate());
            BigDecimal walletBalance = leverWallet.getBalance().add(leverWallet.getFrozenBalance());
            leverWallet.setFoldBtc(walletBalance.multiply(coinRate.getUsdRate()).divide(btcCoin.getUsdRate(), 8, BigDecimal.ROUND_DOWN));
        }
        //已借贷总金额，单位美元
        BigDecimal totalLoanAmountUsd = BigDecimal.ZERO;
        //已接待金额加利息，单位美元
        BigDecimal totalLoanAmountAndAccumulativeUsd = BigDecimal.ZERO;
        BigDecimal baseLoanCount = BigDecimal.ZERO;
        BigDecimal coinLoanCount = BigDecimal.ZERO;
        BigDecimal baseAccumulativeCount = BigDecimal.ZERO;
        BigDecimal coinAccumulativeCount = BigDecimal.ZERO;
        for (LoanRecord loanRecord : loanRecordList) {
            CoinExchangeFactory.ExchangeRate coinRate = coinExchangeFactory.get(loanRecord.getCoin().getUnit());
            totalLoanAmountUsd = totalLoanAmountUsd.add(loanRecord.getAmount()).multiply(coinRate.getUsdRate());
            totalLoanAmountAndAccumulativeUsd = totalLoanAmountAndAccumulativeUsd
                    .add(loanRecord.getAmount().add(loanRecord.getAccumulative()).multiply(coinRate.getUsdRate()));
            if (loanRecord.getCoin().getUnit().equals(leverCoin.getBaseSymbol())) {
                baseLoanCount = baseLoanCount.add(loanRecord.getAmount());
                baseAccumulativeCount = baseAccumulativeCount.add(loanRecord.getAccumulative());
            } else if (loanRecord.getCoin().getUnit().equals(leverCoin.getCoinSymbol())) {
                coinLoanCount = coinLoanCount.add(loanRecord.getAmount());
                coinAccumulativeCount = coinAccumulativeCount.add(loanRecord.getAccumulative());
            }
        }
        vo.setBaseLoanCount(baseLoanCount);
        vo.setBaseAccumulativeCount(baseAccumulativeCount);
        vo.setCoinLoanCount(coinLoanCount);
        vo.setCoinAccumulativeCount(coinAccumulativeCount);
        //持有金额减去已借贷金额，是为实际的本金
        BigDecimal totalCanLoan = totalCanLoanAmountUsd.subtract(totalLoanAmountUsd);
        //本金乘以(倍率-1)，减去已借贷金额和利息，结果为可借贷金额
        totalCanLoan = totalCanLoan.multiply(vo.getProportion().subtract(BigDecimal.ONE)).subtract(totalLoanAmountAndAccumulativeUsd);
        if (totalCanLoan.compareTo(BigDecimal.ZERO) > 0) {
            vo.setBaseCanLoan(totalCanLoan.divide(coinExchangeFactory.get(leverCoin.getBaseSymbol()).getUsdRate(), 8, BigDecimal.ROUND_DOWN));
            vo.setCoinCanLoan(totalCanLoan.divide(coinExchangeFactory.get(leverCoin.getCoinSymbol()).getUsdRate(), 8, BigDecimal.ROUND_DOWN));
        } else {
            vo.setBaseCanLoan(BigDecimal.ZERO);
            vo.setCoinCanLoan(BigDecimal.ZERO);
        }

    }


    /**
     * 计算风险率
     *
     * @param memberId
     * @param leverCoinId
     * @return
     */
    public InspectBean getCalculatedRisk(Long memberId, Long leverCoinId) {
        log.info("用户" + memberId + "币对ID:" + leverCoinId);
        InspectBean inspectBean = new InspectBean();
        LeverCoin leverCoin = leverCoinService.findOne(leverCoinId);
        Member member = memberService.findOne(memberId);
        inspectBean.setLeverCoin(leverCoin);
        inspectBean.setMember(member);
        inspectBean.setMemberId(memberId);
        List<LoanRecord> loanRecordList = loanRecordService.findByMemberIdAndLeverCoinAndRepayment(memberId, leverCoin, BooleanEnum.IS_FALSE);
        List<LeverWallet> leverWalletList = leverWalletService.findByMemberIdAndLeverCoin(memberId, leverCoin);
        if (loanRecordList != null && loanRecordList.size() > 0) {
            if (leverWalletList != null && leverWalletList.size() > 0) {
                //借款金额
                BigDecimal totalLoan = BigDecimal.ZERO;
                //钱包总金额
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (LoanRecord loanRecord : loanRecordList) {
                    CoinExchangeFactory.ExchangeRate coinRate = coinExchangeFactory.get(loanRecord.getCoin().getUnit());
                    BigDecimal amount = coinRate.getUsdRate().multiply(loanRecord.getAmount().add(loanRecord.getAccumulative()));
                    totalLoan = totalLoan.add(amount);
                }
                for (LeverWallet leverWallet : leverWalletList) {
                    CoinExchangeFactory.ExchangeRate coinRate = coinExchangeFactory.get(leverWallet.getCoin().getUnit());
                    BigDecimal amount = coinRate.getUsdRate().multiply(leverWallet.getBalance().add(leverWallet.getFrozenBalance()));
                    totalAmount = totalAmount.add(amount);
                }
                //钱包余额除以借贷金额等于风险率
                if (totalLoan.compareTo(BigDecimal.ZERO) > 0) {
                    log.info("总资产==============" + totalAmount);
                    log.info("借贷资产==============" + totalLoan);
                    inspectBean.setRiskRate(totalAmount.multiply(new BigDecimal(100)).divide(totalLoan, 8, BigDecimal.ROUND_DOWN));
                } else {
                    //无借贷用户
                    inspectBean.setRiskRate(BigDecimal.ZERO);
                }
            } else {
                //标识已经冻结了钱包的用户
                inspectBean.setRiskRate(BigDecimal.ZERO);
            }
        } else {
            inspectBean.setRiskRate(BigDecimal.ZERO);
        }
        return inspectBean;
    }

}
