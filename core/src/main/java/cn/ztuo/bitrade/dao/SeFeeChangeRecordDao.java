package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.SeFeeChangeRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author MrGao
 * @Description:
 * @date 2018/5/410:18
 */
public interface SeFeeChangeRecordDao extends BaseDao<SeFeeChangeRecord> {

    @Query("update SeFeeChangeRecord set status = 1 where memberId = :memberId")
    @Modifying
    int updateStatus(@Param(value = "memberId") Long memberId);
}
