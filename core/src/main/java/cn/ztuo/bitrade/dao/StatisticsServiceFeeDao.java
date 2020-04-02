package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.statistics.StatisticsServiceFee;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

/**
 * @author Paradise
 */
public interface StatisticsServiceFeeDao extends BaseDao<StatisticsServiceFee> {

    /**
     * 汇总查询
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @param currency  币种
     * @return 查询结果
     */
    @Query(value = "SELECT sum(coin_fee) coin_fee,sum(legal_fee) legal_fee,avg(coin_fee) con_fee_avg, avg(legal_fee) legal_fee_avg " +
            "FROM statistics_service_fee s WHERE " +
            " if(?3 is not null and ?3 != '', s.currency = ?3, 1) " +
            " and if(?1 is not null, s.date_ >= ?1, 1) and if(?2 is not null, s.date_ <= ?2, 1)", nativeQuery = true)
    Map<String, String> selectSum(String startDate, String endDate, String currency);
}
