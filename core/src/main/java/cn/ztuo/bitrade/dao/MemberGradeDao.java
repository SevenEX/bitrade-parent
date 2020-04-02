package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.MemberGrade;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @description: MemberGradeDao
 * @author: MrGao
 * @create: 2019/04/25 15:54
 */
@Repository
public interface MemberGradeDao extends BaseDao<MemberGrade> {

    @Query("update MemberGrade set exchangeFeeRate = :exchangeFeeRate,exchangeMakerFeeRate = :exchangeMakerFeeRate ")
    @Modifying
    int updateMemberGrade(@Param("exchangeFeeRate")BigDecimal exchangeFeeRate, @Param("exchangeMakerFeeRate")BigDecimal exchangeMakerFeeRate);

    @Query("update MemberGrade set otcFeeRate = :otcFeeRate ")
    @Modifying
    int updateOtcFee(@Param("otcFeeRate")BigDecimal otcFeeRate);

    @Query("update MemberGrade set getSeDiscountRate = :seDiscountRate ")
    @Modifying
    int updateSeDiscountRate(@Param("seDiscountRate")BigDecimal seDiscountRate);
}
