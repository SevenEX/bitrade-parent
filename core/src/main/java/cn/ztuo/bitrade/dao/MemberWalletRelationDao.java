package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.MemberWalletRelation;

import java.util.List;

public interface MemberWalletRelationDao extends BaseDao<MemberWalletRelation> {

    /**
     * 查找用户全部跨链钱包地址
     * @return 用户多链钱包地址
     */
    List<MemberWalletRelation> findAllByMemberIdAndCoinId(Long memberId, String coinId);
    List<MemberWalletRelation> findAllByMemberId(Long memberId);

    /**
     * @param coinKey 币种唯一键
     * @param address 地址
     * @return 用户钱包关系
     */
    MemberWalletRelation findByCoinKeyAndAddress(String coinKey, String address);
}
