package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.WithdrawStatus;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.WithdrawRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @author Seven
 * @date 2019年01月29日
 */
public interface WithdrawRecordDao extends BaseDao<WithdrawRecord> {

    @Query(value = "select a.unit , sum(b.total_amount) amount,sum(b.fee) from withdraw_record b,coin a where a.name = b.coin_id and date_format(b.deal_time,'%Y-%m-%d') = :date and b.status = 3 group by a.unit",nativeQuery = true)
    List<Object[]> getWithdrawStatistics(@Param("date") String date);

    long countAllByStatus(WithdrawStatus status);

    @Query("select a from WithdrawRecord a where a.id in (:ids)")
    List<WithdrawRecord> findByIds(@Param("ids") Long[] ids);

    @Query("select sum(w.totalAmount) from WithdrawRecord w where w.status <> 2 and w.coin= :coin and w.createTime between :startTime and :endTime")
    Double countWithdrawAmountByTimeAndMemberIdAndCoin(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("coin") Coin coin);
}
