package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.BusinessAuthApply;
import cn.ztuo.bitrade.entity.Member;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/7
 */
public interface BusinessAuthApplyDao extends BaseDao<BusinessAuthApply> {

    List<BusinessAuthApply> findByMemberOrderByIdDesc(Member member);

    List<BusinessAuthApply> findByMemberAndCertifiedBusinessStatusOrderByIdDesc(Member member, CertifiedBusinessStatus certifiedBusinessStatus);

    long countAllByCertifiedBusinessStatus(CertifiedBusinessStatus status);

}
