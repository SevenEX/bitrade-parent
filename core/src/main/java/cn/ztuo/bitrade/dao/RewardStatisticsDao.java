package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.RewardStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Seven
 * @date 2019年03月08日
 */
public interface RewardStatisticsDao extends BaseDao<RewardStatistics> {

    @Query(value = "SELECT\n" +
            "\trs.create_time as create_time,SUM(rs.amount*c.usd_rate) as amount,rs.order_member_id\n" +
            "FROM\n" +
            "\treward_statistics rs\n" +
            "LEFT JOIN coin c ON c.`name` = rs.coin_id\n" +
            "WHERE rs.member_id = :memberId AND create_time = :createTime\n" +
            "GROUP BY rs.create_time,rs.order_member_id\n" +
            "ORDER BY amount",
            countQuery = "SELECT count(1)\n" +
                    "FROM(SELECT\n" +
                    "\trs.create_time as create_time,SUM(rs.amount*c.usd_rate) as amount,rs.order_member_id\n" +
                    "FROM\n" +
                    "\treward_statistics rs\n" +
                    "LEFT JOIN coin c ON c.`name` = rs.coin_id\n" +
                    "WHERE rs.member_id = :memberId AND create_time = :createTime\n" +
                    "GROUP BY rs.create_time,rs.order_member_id\n" +
                    ") A",
            nativeQuery = true)
    Page<Object[]> findAll(@Param("memberId") long memberId,@Param("createTime") String month,Pageable pageable);

    @Query(value = "SELECT\n" +
            "\trs.sum_date as create_time,SUM(rs.amount*c.usd_rate) as amount,rs.member_id\n" +
            "FROM\n" +
            "\treward_record_sum rs\n" +
            "LEFT JOIN coin c ON c.`name` = rs.coin_id\n" +
            "WHERE rs.sum_date = :createTime\n" +
            "AND type = '0'\n" +
            "GROUP BY rs.sum_date,rs.member_id\n" +
            "ORDER BY amount DESC\n" +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findAllList(@Param("createTime") String month, int limit);

}
