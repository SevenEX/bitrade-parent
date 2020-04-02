package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.statistics.StatisticsWithdraw;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

/**
 * @author Paradise
 */
public interface StatisticsWithdrawDao extends BaseDao<StatisticsWithdraw> {

    /**
     * 汇总查询
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @param currency  币种
     * @return 查询结果
     */
    @Query(value = "SELECT sum(people_count) as people_count,sum(recharge_count) recharge_count,sum(recharge_amount) recharge_amount " +
            "FROM statistics_withdraw s WHERE if(?3 != '' and ?3 is not null ,s.currency = ?3,1) and " +
            "if(?1 is not null, s.date_ >= ?1, 1) and if(?2 is not null, s.date_ <= ?2, 1)", nativeQuery = true)
    Map<String, String> selectSum(String startDate, String endDate, String currency);
}
