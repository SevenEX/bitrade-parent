package cn.ztuo.bitrade.dao;


import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.MemberTransaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Date;

public interface MemberTransactionDao extends BaseDao<MemberTransaction> {

    @Query("select t.symbol as symbol,sum(t.amount) as amount from MemberTransaction t where  t.amount > 0 and t.memberId = :memberId and t.type in :types GROUP BY t.symbol")
    List<Map<String,Object>> findTransactionSum(@Param("memberId") Long memberId, @Param("types") List<TransactionType> types);

    @Query("select sum(t.amount)  as amount from MemberTransaction t where t.flag = 0  and t.memberId = :memberId and t.symbol = :symbol and t.type = :type and t.createTime >= :startTime and t.createTime <= :endTime")
    Map<String,Object> findMatchTransactionSum(@Param("memberId") Long memberId, @Param("symbol") String symbol, @Param("type") TransactionType type, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    @Query("select sum(t.amount)  as amount from MemberTransaction t where t.flag = 0  and t.symbol = :symbol and t.type = :type and t.createTime >= :startTime and t.createTime <= :endTime")
    Map<String,Object> findMatchTransactionSum(@Param("symbol") String symbol, @Param("type") TransactionType type, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 根据时间预估分红的数量
     * @param beginDate
     * @param endDate
     * @return
     */
    @Query(value = "SELECT\n" +
            "\t*\n" +
            "FROM\n" +
            "\tmember_transaction\n" +
            "WHERE\n" +
            "\tcreate_time BETWEEN :beginDate\n" +
            "AND :endDate\n" +
            "AND fee > 0",nativeQuery=true)
    List<MemberTransaction> findAllByCreateTime(@Param("beginDate") String beginDate, @Param("endDate") String endDate);
}
