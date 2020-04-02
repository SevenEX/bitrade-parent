package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.statistics.StatisticsExchange;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author Paradise
 */
public interface StatisticsExchangeDao extends BaseDao<StatisticsExchange> {

    /**
     * 汇总查询 - 币币交易交易量
     *
     * @param date 日期 null 查询全部汇总
     * @param unit 币种
     * @return 查询结果
     */
    @Query(value = "select max(id) id, now() create_time, max(date_) date_, coin_symbol, base_symbol, " +
            " sum(amount) amount, sum(money) money, sum(money_usd) money_usd  from statistics_exchange" +
            " where coin_symbol = ?2" +
            " and if(?1 is not null, date_ = ?1, 1) group by coin_symbol,base_symbol", nativeQuery = true)
    List<StatisticsExchange> selectSumAmountAndMoneyUsd(String date, String unit);

    /**
     * 图表查询
     *
     * @param unit 币种
     * @return 查询结果
     */
    @Query(value = "select max(id) id, now() create_time, sum(amount) amount, sum(money) money, base_symbol," +
            " coin_symbol, sum(money_usd) money_usd, date_ from statistics_exchange" +
            " where coin_symbol = ?3" +
            " and if(?1 is not null, date_ >= ?1, 1)" +
            " and if(?2 is not null, date_ <= ?2, 1) group by date_, coin_symbol, base_symbol", nativeQuery = true)
    List<StatisticsExchange> selectSum(Date startDate, Date endDate, String unit);
}
