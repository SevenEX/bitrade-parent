package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.dto.MemberBonusDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @author: Seven
 * @date: create in 16:20 2018/6/30
 * @Modified:
 */
public interface MemberBonusDao extends BaseDao<MemberBonusDTO> {

//    @Query(value = "SELECT id,member_id,have_time,arrive_time,mem_bouns,coin_id from member_bonus where member_id=:memberId",nativeQuery = true)
    @Query(value = "SELECT * from member_bonus  where member_id=:memberId ORDER BY id DESC " ,nativeQuery = true)
    List<MemberBonusDTO> getBonusByMemberId(@Param("memberId")long memberId);

    @Query(value = "SELECT SUM(mem_bouns) from member_bonus  where member_id=:memberId" ,nativeQuery = true)
    BigDecimal getBonusAmountByMemberId(@Param("memberId")long memberId);
    
}
