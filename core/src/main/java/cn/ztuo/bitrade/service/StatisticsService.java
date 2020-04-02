package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.OrderStatus;
import cn.ztuo.bitrade.dao.*;
import cn.ztuo.bitrade.entity.statistics.*;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.EnumHelperUtil;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.ztuo.bitrade.entity.statistics.QStatisticsRegister.statisticsRegister;

/**
 * @author MrGao
 * @description 统计service
 * @date 2018/1/8 16:21
 */
@Service
@Slf4j
public class StatisticsService extends BaseService {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberDao memberDao;
    @Autowired
    private OrderDao orderDao;
    @Resource
    private StatisticsRechargeDao statisticsRechargeDao;
    @Resource
    private StatisticsRegisterDao statisticsRegisterDao;
    @Resource
    private StatisticsTransactionDao statisticsTransactionDao;
    @Resource
    private StatisticsWithdrawDao statisticsWithdrawDao;
    @Resource
    private StatisticsServiceFeeDao statisticsServiceFeeDao;
    @Resource
    private StatisticsOtcDao statisticsOtcDao;
    @Resource
    private StatisticsExchangeDao statisticsExchangeDao;

    private static List<String> getDateStrList(String startDate, String endDate) throws ParseException {
        List<String> dateStrList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date sd = sdf.parse(startDate);
        Date ed = sdf.parse(endDate);
        while (sd.before(ed)) {
            dateStrList.add(sdf.format(sd));
            sd = DateUtil.dateAddDay(sd, 1);
        }
        dateStrList.add(sdf.format(ed));
        return dateStrList;
    }

    private static List<String> getDateStrList(Date sd, Date ed) {
        List<String> dateStrList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        while (sd.before(ed)) {
            dateStrList.add(sdf.format(sd));
            sd = DateUtil.dateAddDay(sd, 1);
        }
        dateStrList.add(sdf.format(ed));
        return dateStrList;
    }

    public static void main(String[] args) throws ParseException {
        List<String> dl = getDateStrList("2020-03-01", "2020-3-14");
        for (String s : dl) {
            System.out.println(s);
        }
    }

    /**
     * 统计控制台信息
     *
     * @return Map<String, Integer>
     */
    public Map<String, Integer> getIndexMemberStatistics() {
        Map<String, Integer> map = new HashMap<>(8);
        map.put("applicationNum", memberDao.getApplicationSum());
        map.put("businessNum", memberDao.getBusinessSum());
        map.put("registrationNum", memberDao.getRegistrationSum());
        String date = DateUtil.dateAddDay(-1);
        map.put("yesterdayRegistrationNum", memberDao.getRegistrationNum(date));
        map.put("yesterdayApplicationNum", memberDao.getApplicationNum(date));
        map.put("yesterdayBusinessNum", memberDao.getBusinessNum(date));
        return map;
    }

    /**
     * 统计控制台信息
     *
     * @return Map<String, Integer>
     */
    public List<Map<String, Object>> getIndexMemberStatisticsChart(String startDate, String endDate) {
        List<Map<String, String>> applicationNumList = memberDao.getApplicationNumList(startDate, endDate);
        Map<String, String> applicationMap = list2Map(applicationNumList);
        List<Map<String, String>> businessNumList = memberDao.getBusinessNumList(startDate, endDate);
        Map<String, String> businessMap = list2Map(businessNumList);
        List<Map<String, String>> registrationNumList = memberDao.getRegistrationNumList(startDate, endDate);
        Map<String, String> registerMap = list2Map(registrationNumList);
        List<Map<String, Object>> resList = new ArrayList<>();
        List<String> dateStrList = new ArrayList<>();
        try {
            dateStrList = getDateStrList(startDate, endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<String, Object> itemMap;
        for (String date_ : dateStrList) {
            itemMap = new HashMap<>();
            itemMap.put("date", date_);
            itemMap.put("registrationNum", registerMap.getOrDefault(date_, "0"));
            itemMap.put("applicationNum", applicationMap.getOrDefault(date_, "0"));
            itemMap.put("businessNum", businessMap.getOrDefault(date_, "0"));
            resList.add(itemMap);
        }
        return resList;
    }

    private Map<String, String> list2Map(List<Map<String, String>> list) {
        Map<String, String> map = new HashMap<>();
        for (Map<String, String> item : list) {
            map.put(item.get("date_"), item.getOrDefault("count_", "0"));
        }
        return map;
    }

    public Page<StatisticsRecharge> findAllStatisticsRecharge(Predicate predicate, Pageable pageable) {
        return statisticsRechargeDao.findAll(predicate, pageable);
    }


    public Page<StatisticsRegister> findAllStatisticsRegister(Date startDate, Date endDate, Pageable pageable) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<BooleanExpression> l = new ArrayList<>();
        if (startDate != null && endDate != null) {
            l.add(statisticsRegister.date.gt(sdf.format(startDate)).or(statisticsRegister.date.eq(sdf.format(startDate))));
            l.add(statisticsRegister.date.lt(sdf.format(endDate)).or(statisticsRegister.date.eq(sdf.format(endDate))));
        }
        return statisticsRegisterDao.findAll(PredicateUtils.getPredicate(l), pageable);
    }

    public Page<StatisticsTransaction> findAllStatisticsTransaction(Predicate predicate, Pageable pageable) {
        return statisticsTransactionDao.findAll(predicate, pageable);
    }

    public Page<StatisticsWithdraw> findAllStatisticsWithdraw(Predicate predicate, Pageable pageable) {
        return statisticsWithdrawDao.findAll(predicate, pageable);
    }

    public Page<StatisticsServiceFee> findAllStatisticsServiceFee(Predicate predicate, Pageable pageable) {
        return statisticsServiceFeeDao.findAll(predicate, pageable);
    }


    /**
     * @param sql 需要包含startTime endTime 两个占位符
     * @author MrGao
     * @description 获取统计数据
     * @date 2018/1/9 15:25
     */
    public List getStatistics(String startTime, String endTime, String sql) {
        Query query = em.createNativeQuery(sql);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);
        List resultList = query.getResultList();
        return resultList;
    }

    /**
     * 统计最近几日注册人数
     *
     * @param day
     * @return
     */
    public int getLatelyRegMember(int day) {
        Date startTime = DateUtil.strToDate(DateUtil.getPastDate(day) + " 00:00:00");
        Date endTime = DateUtil.strToDate(DateUtil.getDate() + " 23:59:59");
        return memberDao.countByRegistrationTimeBetween(startTime, endTime);
    }

    /**
     * 查询时间段内订单数量 status为-1代表所有的状态
     *
     * @param startTime
     * @param endTime
     * @param status
     * @return
     */
    public int getLatelyOrder(String startTime, String endTime, int status) {
        Date startTimeDate = DateUtil.strToDate(startTime + " 00:00:00");
        Date endTimeDate = DateUtil.strToDate(endTime + " 23:59:59");
        if (status < 0) {
            return orderDao.countByCreateTimeBetween(startTimeDate, endTimeDate);
        }
        OrderStatus orderStatus = EnumHelperUtil.indexOf(OrderStatus.class, status);
        return orderDao.countByStatusAndCreateTimeBetween(orderStatus, startTimeDate, endTimeDate);
    }

    //根据状态统计订单
    public int getLatelyOrder(OrderStatus status) {
        return orderDao.countByStatus(status);
    }

    public int getLatelyAdvertise(int i) {

        return 0;
    }

    /**
     * 汇总查询 - 充币统计
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @param currency  币种
     * @return 查询结果
     */
    public Map<String, String> rechargeSumQuery(String startDate, String endDate, String currency) {
        if (StringUtils.isEmpty(startDate)) {
            startDate = null;
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = null;
        }
        return statisticsRechargeDao.selectSum(startDate, endDate, currency);
    }

    /**
     * 汇总查询 - 注册统计
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @return 查询结果
     */
    public Map<String, String> registerSumQuery(String startDate, String endDate) {
        if (StringUtils.isEmpty(startDate)) {
            startDate = null;
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = null;
        }
        return statisticsRegisterDao.selectSum(startDate, endDate);
    }

    /**
     * 汇总查询 - 提币统计
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @param currency  币种
     * @return 查询结果
     */
    public Map<String, String> withdrawSumQuery(String startDate, String endDate, String currency) {
        if (StringUtils.isEmpty(startDate)) {
            startDate = null;
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = null;
        }
        return statisticsWithdrawDao.selectSum(startDate, endDate, currency);
    }

    /**
     * 汇总查询 - 交易统计
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @param symbol    币对
     * @return 查询结果
     */
    public Map<String, String> transactionSumQuery(String startDate, String endDate, String symbol) {
        if (StringUtils.isEmpty(startDate)) {
            startDate = null;
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = null;
        }
        return statisticsTransactionDao.selectSum(startDate, endDate, symbol);
    }

    /**
     * 汇总查询 - 手续费
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @return 查询结果
     */
    public Map<String, String> serviceFeeSumQuery(String startDate, String endDate, String currency) {
        if (StringUtils.isEmpty(startDate)) {
            startDate = null;
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = null;
        }
        return statisticsServiceFeeDao.selectSum(startDate, endDate, currency);
    }

    /**
     * 法币交易 数据统计 - 包含昨日数据和累计数据
     *
     * @param date 日期
     * @param unit 币种
     * @return Map<String, StatisticsOtc>
     */
    public Map<String, StatisticsOtc> otcQuery(Date date, String unit) {
        if (date == null) {
            date = DateUtil.getDate(new Date(), 1);
        }
        Map<String, StatisticsOtc> resMap = new HashMap<>(2);
        resMap.put("yesterday", statisticsOtcDao.selectSum(
                new SimpleDateFormat("yyyy-MM-dd").format(date), unit));
        resMap.put("sum", statisticsOtcDao.selectSum(null, unit));
        return resMap;
    }

    /**
     * 法币交易 数据统计 - 包含昨日数据和累计数据
     *
     * @param unit 币种
     * @return List<StatisticsOtc>
     */
    public List<StatisticsOtc> otcListQuery(Date startDate, Date endDate, String unit) {
        List<String> dateStrList = getDateStrList(startDate, endDate);
        List<StatisticsOtc> queryResList = statisticsOtcDao.selectSum(startDate, endDate, unit);
        List<StatisticsOtc> resList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        // 增加数据为空时 补零处理
        Map<String, StatisticsOtc> statisticsOtcMap = new HashMap<>();
        for (StatisticsOtc item : queryResList) {
            statisticsOtcMap.put(sdf.format(item.getDate()), item);
        }
        try {
            for (String dateStr : dateStrList) {
                if (statisticsOtcMap.get(dateStr) != null) {
                    resList.add(statisticsOtcMap.get(dateStr));
                } else {
                    resList.add(new StatisticsOtc(sdf.parse(dateStr), unit));
                }
            }
        } catch (ParseException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return resList;
    }


    /**
     * 币币交易 数据统计 - 包含昨日数据和累计数据
     *
     * @param unit 币种
     * @return List<StatisticsExchange>
     */
    public List<StatisticsExchange> exchangeListQuery(Date startDate, Date endDate, String unit) {
        List<String> dateStrList = getDateStrList(startDate, endDate);
        return statisticsExchangeDao.selectSum(startDate, endDate, unit);
    }

    /**
     * 币币交易 交易量 数据统计 - 包含昨日数据和累计数据
     *
     * @param date 日期
     * @param unit 币种
     * @return Map<String, StatisticsOtc>
     */
    public List<StatisticsExchange> exchangeQuery(Date date, String unit) {
        if (date == null) {
            return statisticsExchangeDao.selectSumAmountAndMoneyUsd(null, unit);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return statisticsExchangeDao.selectSumAmountAndMoneyUsd(format.format(date), unit);
    }
}
