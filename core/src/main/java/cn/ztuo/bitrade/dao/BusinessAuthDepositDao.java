package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.BusinessAuthDeposit;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/5
 */
public interface BusinessAuthDepositDao extends BaseDao<BusinessAuthDeposit> {
    public List<BusinessAuthDeposit> findAllByStatus(CommonStatus status);
}
