package cn.ztuo.bitrade.job;

import cn.ztuo.bitrade.constant.TransactionTypeEnum;
import cn.ztuo.bitrade.dao.*;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.statistics.StatisticsServiceFee;
import cn.ztuo.bitrade.service.OrderService;
import cn.ztuo.bitrade.util.DateUtil;
import com.google.common.collect.ArrayListMultimap;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Component
@Slf4j
public class MemberStatisticsJob {

    private static Logger logger = LoggerFactory.getLogger(MemberStatisticsJob.class);

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ExchangeOrderRepository exchangeOrderRepository;

    @Autowired
    private MemberDepositDao memberDepositDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private WithdrawRecordDao withdrawRecordDao;

    @Autowired
    private MemberLogDao memberLogDao;

    @Autowired
    private MemberWalletSeHistoryDao memberWalletSeHistoryDao;

    @Autowired
    private MemberGradeDao memberGradeDao;

    @Resource
    private StatisticsServiceFeeDao statisticsServiceFeeDao;
    @Resource
    private CoinDao coinDao;

    /**
     * 交易，统计信息 MongoDB 查询
     *
     * @param type {@link TransactionTypeEnum}
     * @return List<TurnoverStatistics> 多个币种的数据列表
     */
    private List<TurnoverStatistics> txQueryByType(TransactionTypeEnum type) {
        org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.
                core.query.Query(Criteria.where("type").is(type.toString())
                .and("date").is(DateUtil.getDate(new Date(), 1)));
        return mongoTemplate.find(query, TurnoverStatistics.class, "turnover_statistics");
    }

    /**
     * 定时任务：数据统计 - 手续费统计
     */
//    @Scheduled(cron = "0 0 2 * * ? ")
//    public void statisticsServiceFee() {
     @XxlJob("statisticsServiceFee")
     public ReturnT<String> statisticsServiceFee(String param) throws Exception {
        // 查询 比比交易，法币交易 手续费信息
        log.info("--- 定时任务 // 查询 币币交易，法币交易的手续费信息；执行时间：{}", new Date());
        List<Coin> coinList = coinDao.findAll();
        List<TurnoverStatistics> otcList = txQueryByType(TransactionTypeEnum.OTC_NUM);
        List<TurnoverStatistics> exchangeList = txQueryByType(TransactionTypeEnum.EXCHANGE);
        List<StatisticsServiceFee> feeList = new ArrayList<>();
        Map<String, BigDecimal> otcFeeMap = new HashMap<>();
        Map<String, BigDecimal> exchangeFeeMap = new HashMap<>();
        StatisticsServiceFee serviceFee;
        Date date = DateUtil.getDate(new Date(), 1);
        for (TurnoverStatistics statistics : otcList) {
            // 需要叠加
            BigDecimal fee = otcFeeMap.get(statistics.getUnit());
            if (fee == null) {
                otcFeeMap.put(statistics.getUnit(), statistics.getFee());
            } else {
                otcFeeMap.put(statistics.getUnit(), fee.add(statistics.getFee()));
            }
        }
        for (TurnoverStatistics tics : exchangeList) {
            exchangeFeeMap.put(tics.getUnit(), tics.getFee());
        }
        for (Coin coin : coinList) {
            String unit = coin.getUnit();
            serviceFee = new StatisticsServiceFee();
            serviceFee.setCurrency(unit);
            serviceFee.setLegalFee(otcFeeMap.getOrDefault(unit, BigDecimal.ZERO));
            serviceFee.setCoinFee(exchangeFeeMap.getOrDefault(unit, BigDecimal.ZERO));
            serviceFee.setCreateTime(new Date());
            serviceFee.setDate(date);
            feeList.add(serviceFee);
        }
        // 信息入库
        feeList = statisticsServiceFeeDao.saveAll(feeList);
        log.info("数据统计-手续费 数据入库:{}", feeList.size());
        return ReturnT.SUCCESS;
    }

    /**
     * 会员注册/实名/认证商家 统计
     */
//    @Scheduled(cron = "0 34 1 * * ?")
//    public void statisticsMember() {
     @XxlJob("statisticsMember")
     public ReturnT<String> statisticsMember(String param) throws Exception {
        try {
            if (!mongoTemplate.collectionExists("member_log")) {
                List<Date> list = getDateList();
                String dateStr = "";
                for (Date date : list) {
                    dateStr = DateUtil.YYYY_MM_DD.format(date);
                    statisticsMember(dateStr, date);
                }
            } else {
                Date date = DateUtil.dateAddDay(DateUtil.getCurrentDate(), -1);
                String dateStr = DateUtil.getFormatTime(DateUtil.YYYY_MM_DD, date);
                statisticsMember(dateStr, date);
            }
        } catch (ParseException e) {
            logger.error("日期解析异常", e);
        }
         return ReturnT.SUCCESS;
    }

    /**
     * 法币/充币/提币 手续费
     * 币币交易手续费 统计
     * 法币成交量/成交额 统计
     */
//    @Scheduled(cron = "0 24 1 * * ?")
//    public void turnoverStatistics() {
     @XxlJob("turnoverStatistics")
     public ReturnT<String> turnoverStatistics(String param) throws Exception {
        try {
            if (!mongoTemplate.collectionExists("turnover_statistics")) {
                List<Date> list = getDateList();
                String dateStr = "";
                for (Date date : list) {
                    dateStr = DateUtil.YYYY_MM_DD.format(date);
                    statisticsFee(dateStr, date);
                }
            } else {
                Date date = DateUtil.dateAddDay(DateUtil.getCurrentDate(), -1);
                String dateStr = DateUtil.getFormatTime(DateUtil.YYYY_MM_DD, date);
                statisticsFee(dateStr, date);
            }

        } catch (ParseException e) {
            logger.error("日期解析异常", e);
        }
         return ReturnT.SUCCESS;
    }

    /**
     * 币币交易成交量/成交额 统计
     */
//    @Scheduled(cron = "0 14 1 * * ?")
//    public void exchangeStatistics() {
     @XxlJob("exchangeStatistics")
     public ReturnT<String> exchangeStatistics(String param) throws Exception {
        try {
            if (!mongoTemplate.collectionExists("exchange_turnover_statistics")) {
                List<Date> list = getDateList();
                String dateStr = "";
                for (Date date : list) {
                    dateStr = DateUtil.YYYY_MM_DD.format(date);
                    exchangeStatistics(dateStr, date);
                }
            } else {
                Date date = DateUtil.dateAddDay(DateUtil.getCurrentDate(), -1);
                String dateStr = DateUtil.getFormatTime(DateUtil.YYYY_MM_DD, date);
                exchangeStatistics(dateStr, date);
            }

        } catch (ParseException e) {
            logger.error("日期解析异常", e);
        }
         return ReturnT.SUCCESS;
    }

    private void statisticsMember(String dateStr, Date date) throws ParseException {
        logger.info("开始统计会员信息{}", dateStr);
        int registrationNum = memberDao.getRegistrationNum(dateStr);
        int businessNum = memberDao.getBusinessNum(dateStr);
        int applicationNum = memberDao.getApplicationNum(dateStr);
        MemberLog memberLog = new MemberLog();
        memberLog.setApplicationNum(applicationNum);
        memberLog.setBussinessNum(businessNum);
        memberLog.setRegistrationNum(registrationNum);
        memberLog.setDate(DateUtil.YYYY_MM_DD.parse(dateStr));
        memberLog.setYear(DateUtil.getDatePart(date, Calendar.YEAR));
        //Calendar month 默认从0开始，方便起见 保存月份从1开始
        memberLog.setMonth(DateUtil.getDatePart(date, Calendar.MONTH) + 1);
        memberLog.setDay(DateUtil.getDatePart(date, Calendar.DAY_OF_MONTH));
        logger.info("{}会员信息{}", dateStr, memberLog);
        memberLogDao.save(memberLog);
        logger.info("结束统计会员信息{}", dateStr);
    }

    private List<Date> getDateList() throws ParseException {
        List<Date> list = new ArrayList<>();

        Date date = memberDao.getStartRegistrationDate();
        String dateStr = DateUtil.YYYY_MM_DD.format(date);
        date = DateUtil.YYYY_MM_DD.parse(dateStr);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Date endDate = DateUtil.dateAddDay(new Date(), -1);
        while (date.before(endDate)) {
            list.add(date);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            date = calendar.getTime();
        }
        return list;
    }

    private void statisticsFee(String dateStr, Date date) throws ParseException {
        /**
         * 法币成交
         *
         */
        logger.info("开始统计法币成交信息{}", dateStr);
        List<Object[]> list1 = orderService.getOtcOrderStatistics(dateStr);
        TurnoverStatistics turnoverStatistics = new TurnoverStatistics();
        turnoverStatistics.setDate(DateUtil.YYYY_MM_DD.parse(dateStr));
        turnoverStatistics.setYear(DateUtil.getDatePart(date, Calendar.YEAR));
        //Calendar month 默认从0开始，方便起见 保存月份从1开始
        turnoverStatistics.setMonth(DateUtil.getDatePart(date, Calendar.MONTH) + 1);
        turnoverStatistics.setDay(DateUtil.getDatePart(date, Calendar.DAY_OF_MONTH));
        for (Object[] objects : list1) {
            /**
             * 法币成交量/手续费
             */
            turnoverStatistics.setUnit(objects[0].toString());
            turnoverStatistics.setAmount((BigDecimal) objects[2]);
            turnoverStatistics.setFee((BigDecimal) objects[3]);
            turnoverStatistics.setType(TransactionTypeEnum.OTC_NUM);
            logger.info("{}法币成交信息{}", dateStr, turnoverStatistics);
            mongoTemplate.insert(turnoverStatistics, "turnover_statistics");

            /**
             * 法币成交额
             */
            turnoverStatistics.setAmount((BigDecimal) objects[4]);
            turnoverStatistics.setType(TransactionTypeEnum.OTC_MONEY);
            turnoverStatistics.setFee(null);
            mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
        }
        logger.info("结束统计法币成交信息{}", dateStr);

        /**
         * 币币成交额
         */
        logger.info("开始统计币币成交额信息{}", dateStr);
        turnoverStatistics.setFee(null);
        List<Object[]> list2 = exchangeOrderRepository.getExchangeTurnoverBase(dateStr);
        for (Object[] objects : list2) {
            turnoverStatistics.setUnit(objects[0].toString());
            turnoverStatistics.setAmount((BigDecimal) objects[2]);
            turnoverStatistics.setType(TransactionTypeEnum.EXCHANGE_BASE);
            logger.info("{}币币成交额信息{}", dateStr, turnoverStatistics);
            mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
        }
        logger.info("结束统计币币成交额信息{}", dateStr);

        /**
         * 币币成交量
         */
        logger.info("开始统计币币成交量信息{}", dateStr);
        List<Object[]> list3 = exchangeOrderRepository.getExchangeTurnoverCoin(dateStr);
        for (Object[] objects : list3) {
            turnoverStatistics.setUnit(objects[0].toString());
            turnoverStatistics.setAmount((BigDecimal) objects[2]);
            turnoverStatistics.setType(TransactionTypeEnum.EXCHANGE_COIN);
            logger.info("{}币币成交量信息{}", dateStr, turnoverStatistics);
            mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
        }
        logger.info("结束统计币币成交量信息{}", dateStr);

        /**
         * 充币
         */
        logger.info("开始统计充币信息{}", dateStr);
        List<Object[]> list4 = memberDepositDao.getDepositStatistics(dateStr);
        for (Object[] objects : list4) {
            turnoverStatistics.setAmount(new BigDecimal(objects[1].toString()));
            turnoverStatistics.setUnit(objects[0].toString());
            turnoverStatistics.setType(TransactionTypeEnum.RECHARGE);
            logger.info("{}充币信息{}", dateStr, turnoverStatistics);
            mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
        }
        logger.info("结束统计充币信息{}", dateStr);

        /**
         * 币币交易手续费
         */
        logger.info("开始统计币币交易手续费信息{}", dateStr);
        ProjectionOperation projectionOperation = Aggregation.project("time", "type", "unit", "fee");

        Criteria operator = Criteria.where("coinName").ne("").andOperator(
                Criteria.where("time").gte(DateUtil.YYYY_MM_DD_MM_HH_SS.parse(dateStr + " 00:00:00").getTime() / 1000),
                Criteria.where("time").lte(DateUtil.YYYY_MM_DD_MM_HH_SS.parse(dateStr + " 23:59:59").getTime() / 1000),
                Criteria.where("type").is("EXCHANGE")
        );

        MatchOperation matchOperation = Aggregation.match(operator);

        GroupOperation groupOperation = Aggregation.group("unit", "type").sum("fee").as("feeSum");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation, matchOperation, groupOperation);
        // 执行操作
        AggregationResults<Map> aggregationResults = this.mongoTemplate.aggregate(aggregation, "order_detail_aggregation", Map.class);
        List<Map> list = aggregationResults.getMappedResults();
        for (Map map : list) {
            logger.info("*********{}币币交易手续费{}************", dateStr, map);
            turnoverStatistics.setFee(new BigDecimal(map.get("feeSum").toString()));
            turnoverStatistics.setAmount(null);
            turnoverStatistics.setUnit(map.get("unit").toString());
            turnoverStatistics.setType(TransactionTypeEnum.EXCHANGE);
            logger.info("{}币币交易手续费信息{}", dateStr, turnoverStatistics);
            mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
        }
        logger.info("结束统计币币交易手续费信息{}", dateStr);

        /**
         * 提币
         */
        logger.info("开始统计提币信息{}", dateStr);
        List<Object[]> list5 = withdrawRecordDao.getWithdrawStatistics(dateStr);
        for (Object[] objects : list5) {
            turnoverStatistics.setFee(new BigDecimal(objects[2].toString()));
            turnoverStatistics.setAmount(new BigDecimal(objects[1].toString()));
            turnoverStatistics.setUnit(objects[0].toString());
            turnoverStatistics.setType(TransactionTypeEnum.WITHDRAW);
            logger.info("{}提币信息{}", dateStr, turnoverStatistics);
            mongoTemplate.insert(turnoverStatistics, "turnover_statistics");
        }
        logger.info("结束统计提币信息{}", dateStr);
    }


    /**
     * 币币交易数据统计
     *
     * @param dateStr 日期字符串 昨日
     * @param date    日期 昨日
     * @throws ParseException 解析异常
     */
    private void exchangeStatistics(String dateStr, Date date) throws ParseException {
        /*
         * 币币成交(按照交易对统计) warning: group by complete_time
         */
        logger.info("开始统计币币成交(按照交易对统计)信息{}", dateStr);
        List<Object[]> list = exchangeOrderRepository.getExchangeTurnoverSymbol(dateStr);
        ExchangeTurnoverStatistics exchangeTurnoverStatistics = new ExchangeTurnoverStatistics();
        for (Object[] objects : list) {
            exchangeTurnoverStatistics.setDate(DateUtil.YYYY_MM_DD.parse(dateStr));
            exchangeTurnoverStatistics.setAmount((BigDecimal) objects[3]);
            exchangeTurnoverStatistics.setBaseSymbol((String) objects[0]);
            exchangeTurnoverStatistics.setCoinSymbol((String) objects[1]);
            exchangeTurnoverStatistics.setMoney((BigDecimal) objects[4]);
            exchangeTurnoverStatistics.setYear(DateUtil.getDatePart(date, Calendar.YEAR));
            //Calendar month 默认从0开始，方便起见 保存月份从1开始
            exchangeTurnoverStatistics.setMonth(DateUtil.getDatePart(date, Calendar.MONTH) + 1);
            exchangeTurnoverStatistics.setDay(DateUtil.getDatePart(date, Calendar.DAY_OF_MONTH));
            logger.info("{}币币成交(按照交易对统计)信息{}", dateStr, exchangeTurnoverStatistics);
            mongoTemplate.insert(exchangeTurnoverStatistics, "exchange_turnover_statistics");
        }
        logger.info("结束统计币币成交(按照交易对统计)信息{}", dateStr);
    }

    /**
     * 定时任务：用户持有SE升降级逻辑
     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    @Transactional
//    public void memberUpgradeLogic() {
     @XxlJob("memberUpgradeLogic")
     @Transactional
     public ReturnT<String> memberUpgradeLogic(String param) throws Exception {
        int page = 0;
        int pageSize = 100;
        long startMemberId = 0L;
        Page<MemberWalletSeHistory> walletSeHistories;
        List<MemberGrade> memberGradeList = memberGradeDao.findAll();
        memberGradeList.sort((a, b) -> b.getGradeBound().compareTo(a.getGradeBound()));

        Date currentDate = new Date();
        do {
            Pageable pageable = PageRequest.of(page, pageSize);
            Specification<MemberWalletSeHistory> specification = (Specification<MemberWalletSeHistory>) (root, query, criteriaBuilder) -> {
                List<Predicate> list = new ArrayList<>();
                Predicate p1 = criteriaBuilder.greaterThanOrEqualTo(root.get("memberId"), startMemberId);
                list.add(p1);
                Predicate p2 = criteriaBuilder.between(root.get("createTime"), DateUtil.dateAddDay(currentDate, -2), DateUtil.dateAddDay(currentDate, -1));
                list.add(p2);
                return criteriaBuilder.and(list.toArray(new Predicate[0]));
            };
            walletSeHistories = memberWalletSeHistoryDao.findAll(specification, pageable);
            ArrayListMultimap<Long, Long> gradeMemberMap = ArrayListMultimap.create();
            for (MemberWalletSeHistory dto : walletSeHistories) {
                memberGradeList.stream().filter(grade -> dto.getBalance().compareTo(BigDecimal.valueOf(grade.getGradeBound())) >= 0).findFirst()
                        .ifPresent(memberGrade -> gradeMemberMap.put(memberGrade.getId(), dto.getMemberId()));
            }
            gradeMemberMap.asMap().forEach((k, v) -> {
                memberDao.updateMemberGrades(v, k);
            });
            page++;
        } while (!walletSeHistories.isEmpty());
         return ReturnT.SUCCESS;
    }

}
