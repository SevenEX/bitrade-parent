package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.entity.ExchangeOrder;
import cn.ztuo.bitrade.entity.ExchangeOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExchangeOrderRepository extends JpaRepository<ExchangeOrder, String>, JpaSpecificationExecutor<ExchangeOrder>, QuerydslPredicateExecutor<ExchangeOrder> {
    ExchangeOrder findByOrderId(String orderId);

    @Modifying
    @Query("update ExchangeOrder exchange set exchange.tradedAmount = exchange.tradedAmount + ?1  where exchange.orderId = ?2")
    int increaseTradeAmount(BigDecimal amount, String orderId);

    @Modifying
    @Query("update ExchangeOrder  exchange set exchange.status = :status where exchange.orderId = :orderId")
    int updateStatus(@Param("orderId") String orderId, @Param("status") ExchangeOrderStatus status);

    @Query(value="select coin_symbol unit,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(traded_amount) amount from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by unit,completed_time",nativeQuery = true)
    List<Object[]> getExchangeTurnoverCoin(@Param("date") String date);

    @Query(value="select base_symbol unit,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(turnover) amount from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by unit,completed_time",nativeQuery = true)
    List<Object[]> getExchangeTurnoverBase(@Param("date") String date);

    @Query(value="select base_symbol , coin_symbol,FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d'),sum(traded_amount),sum(turnover) from exchange_order where FROM_UNIXTIME(completed_time/1000, '%Y-%m-%d') = :date and direction = 1 and status = 1 group by base_symbol,coin_symbol,completed_time",nativeQuery = true)
    List<Object[]> getExchangeTurnoverSymbol(@Param("date") String date) ;

    /**
     * 查询该用户近30天的交易量
     * @param memberId
     * @return
     */
    @Query("SELECT count(a.orderId) FROM ExchangeOrder a WHERE  a.memberId = :memberId AND a.completedTime BETWEEN :beginDate AND :endDate")
    int countExchangeOrderByMemberId(@Param("memberId") Long memberId, @Param("beginDate") Long beginDate, @Param("endDate") Long endDate);
    List<ExchangeOrder> findAllByMemberIdAndMarginTradeAndStatus(Long memberId, BooleanEnum marginTrade, ExchangeOrderStatus status);

    @Query(value = "select o.memberId,count(o.memberId) as c from ExchangeOrder o where o.time between :startTime and :endTime GROUP BY o.memberId")
    List<Object[]> countOrdersByMemberIdAndCreateTime(@Param("startTime") Long startTime, @Param("endTime") Long endTime);


    @Query(value = "select exchange from ExchangeOrder exchange where exchange.time< :cancleTime and exchange.status=0 and exchange.orderResource=0")
    List<ExchangeOrder> queryExchangeOrderByTimeById(@Param("cancleTime") long cancelTime);

    @Transactional
    @Modifying
    @Query(value = "update exchange_order set status=0 where order_id=:orderId and status=4",nativeQuery = true)
    int pushWaitingOrderByOrderId(@Param("orderId") String orderId);

    @Query(value = "select * from exchange_order where order_id = :orderId for update", nativeQuery = true)
    Optional<ExchangeOrder> getOrderForUpdate(@Param("orderId") String orderId);
}
