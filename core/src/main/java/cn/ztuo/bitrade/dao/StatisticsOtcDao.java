package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.statistics.StatisticsOtc;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author Paradise
 */
public interface StatisticsOtcDao extends BaseDao<StatisticsOtc> {

    /**
     * 汇总查询
     *
     * @param date 日期 null 查询全部汇总
     * @param unit 币种
     * @return 查询结果
     */
    @Query(value = "select max(id) id, now() create_time, sum(amount) amount, " +
            "sum(money) money, sum(fee) fee,max(date_) date_, unit from statistics_otc" +
            " where unit = ?2" +
            " and if(?1 is not null, date_ = ?1, 1) group by unit", nativeQuery = true)
    StatisticsOtc selectSum(String date, String unit);

    @Query(value = "select max(id) id, now() create_time, sum(amount) amount, " +
            "sum(money) money, sum(fee) fee, date_, unit from statistics_otc" +
            " where unit = ?3" +
            " and if(?1 is not null, date_ >= ?1, 1)" +
            " and if(?2 is not null, date_ <= ?2, 1) group by date_, unit", nativeQuery = true)
    List<StatisticsOtc> selectSum(Date startDate, Date endDate, String unit);
}
