package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.MemberApiKey;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @description: MemberApiKeyDao
 * @author: MrGao
 * @create: 2019/05/07 10:41
 */
@Repository
public interface MemberApiKeyDao extends BaseDao<MemberApiKey> {

    MemberApiKey findMemberApiKeyByApiKey(String apiKey);


    @Query("select new cn.ztuo.bitrade.entity.MemberApiKey(a.memberId,a.apiKey,a.bindIp,a.apiName,a.remark,a" +
            ".expireTime,a.id,a.createTime,a.powerLimit,a.status) from MemberApiKey a where a.memberId=:memberId order by a.createTime desc ")
    List<MemberApiKey> findAllByMemberId(@Param("memberId") Long memberId);

    MemberApiKey findMemberApiKeyByMemberIdAndId(@Param("memberId") Long memberId, @Param("id") Long id);

    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("delete from MemberApiKey where id=:id")
    Integer del(@Param("id")Long id);
}
