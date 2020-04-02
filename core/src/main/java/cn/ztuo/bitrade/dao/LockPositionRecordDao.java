package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.LockPositionRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LockPositionRecordDao extends BaseDao<LockPositionRecord> {
    Optional<LockPositionRecord> findById(Long id);

    @Query("select l from LockPositionRecord l where l.status= :status and l.unlockTime < :unlockTime")
    List<LockPositionRecord> findByStatusAndUnlockTime(@Param("status") CommonStatus status, @Param("unlockTime") Date unlockTime);

    @Modifying
    @Query("update LockPositionRecord l set l.status = :status where l.id= :id")
    void unlockById(@Param("id") Long id, @Param("status") CommonStatus status);

    @Modifying
    @Query("update LockPositionRecord l set l.status = :status where l.id in (:ids)")
    void unlockByIds(@Param("ids") List<Long> ids, @Param("status") CommonStatus status);

    List<LockPositionRecord> findByMemberIdAndCoinAndStatus(Long memberId, Coin coin, CommonStatus status);
}
