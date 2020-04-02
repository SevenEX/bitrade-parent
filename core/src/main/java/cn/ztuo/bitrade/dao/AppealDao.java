package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.AppealStatus;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Appeal;
import cn.ztuo.bitrade.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Seven
 * @date 2019年01月23日
 */
public interface AppealDao extends BaseDao<Appeal> {

    @Query("select count(a.id) as complainantNum from Appeal a where a.initiatorId = :memberId")
    Long getBusinessAppealInitiatorIdStatistics(@Param("memberId") Long memberId);

    @Query("select count(a.id) as defendantNum from Appeal a where a.associateId = :memberId")
    Long getBusinessAppealAssociateIdStatistics(@Param("memberId") Long memberId);

    long countAllByStatus(AppealStatus status);

    Appeal findByOrderAndStatus(Order order,AppealStatus status);
}
