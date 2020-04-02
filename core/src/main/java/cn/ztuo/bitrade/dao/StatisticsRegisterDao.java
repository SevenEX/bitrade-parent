package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.statistics.StatisticsRegister;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

/**
 * @author Paradise
 */
public interface StatisticsRegisterDao extends BaseDao<StatisticsRegister> {

    /**
     * 汇总查询
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @return 查询结果
     */
    @Query(value = "SELECT sum(total_count) total_count,sum(self_count) self_count," +
            "sum(invited_count) invited_count, sum(indirect_count) indirect_count " +
            "FROM statistics_register s WHERE " +
            "if(?1 is not null, s.date_ >= ?1, 1) and if(?2 is not null, s.date_ <= ?2, 1)", nativeQuery = true)
    Map<String, String> selectSum(String startDate, String endDate);

}
