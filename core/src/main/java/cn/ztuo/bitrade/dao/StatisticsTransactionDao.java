package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.statistics.StatisticsTransaction;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;

/**
 * @author Paradise
 */
public interface StatisticsTransactionDao extends BaseDao<StatisticsTransaction> {


    /**
     * 汇总查询
     *
     * @param startDate 查询开始时间
     * @param endDate   查询结束时间
     * @param symbol    币对
     * @return 查询结果
     */
    @Query(value = "SELECT sum(people_count) people_count," +
            "sum(purchase_people_count) purchase_people_count," +
            "sum(sell_people_count) sell_people_count, " +
            "sum(transaction_count) transaction_count, " +
            "sum(tx_purchase_count) tx_purchase_count, " +
            "sum(tx_sell_count) tx_sell_count, " +
            "sum(transaction_amount) transaction_amount " +
            "FROM statistics_transaction s WHERE if(?3 != '' and ?3 is not null ,s.symbol = ?3,1) and " +
            "if(?1 is not null, s.date_ >= ?1, 1) and if(?2 is not null, s.date_ <= ?2, 1)", nativeQuery = true)
    Map<String, String> selectSum(String startDate, String endDate, String symbol);
}
