package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.MemberWalletSeHistory;

import java.util.List;

public interface MemberWalletSeHistoryDao extends BaseDao<MemberWalletSeHistory> {

    List<MemberWalletSeHistory> findAllByMemberId(Long memberId);

}
