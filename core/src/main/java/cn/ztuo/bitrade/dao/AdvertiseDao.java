package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.AdvertiseControlStatus;
import cn.ztuo.bitrade.constant.AdvertiseType;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Advertise;
import cn.ztuo.bitrade.entity.Member;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Seven
 * @date 2019年12月07日
 */
public interface AdvertiseDao extends BaseDao<Advertise> {

    List<Advertise> findAllByMemberIdAndStatusNot(Long var1, AdvertiseControlStatus status, Sort var2);

    Advertise findAdvertiseByIdAndMemberIdAndStatusNot(Long var1, Long var2, AdvertiseControlStatus status);

    Advertise findByIdAndMemberId(long v1, long v2);

    @Query(value = "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.price * a.remain_amount >= :money and a.status = 0 and a.advertise_type = 1 and a.price_type = 0 and  FIND_IN_SET(:payMode, a.pay_mode) order by a.price asc, a.top desc, a.create_time asc limit 1", nativeQuery = true)
    Advertise findAdvertiseForEasyBuy(@Param("memberId") Long memberId, @Param("coinId") Long coinId, @Param("money") BigDecimal money, @Param("payMode") String payMode);

    @Query(value = "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.remain_amount >= :amount and a.status = 0 and a.advertise_type = 1 and a.price_type = 0 and  FIND_IN_SET(:payMode, a.pay_mode) order by a.price asc, a.top desc, a.create_time asc limit 1", nativeQuery = true)
    Advertise findAdvertiseForEasyBuyByAmount(@Param("memberId") Long memberId, @Param("coinId") Long coinId, @Param("amount") BigDecimal amount, @Param("payMode") String payMode);

    @Query(value = "select b.* from (" +
            "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.price * a.remain_amount >= :money and a.status = 0 and a.advertise_type = 0  and a.price_type = 0 and  FIND_IN_SET(:payMode, a.pay_mode)" +
            " union all " +
            "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.price * a.remain_amount >= :money and a.status = 0 and a.advertise_type = 0  and a.price_type = 0 and  FIND_IN_SET(:payMode2, a.pay_mode)" +
            " union all " +
            "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.price * a.remain_amount >= :money and a.status = 0 and a.advertise_type = 0  and a.price_type = 0 and  FIND_IN_SET(:payMode3, a.pay_mode)" +
            ") b order by b.price desc, b.top desc, b.create_time asc limit 1", nativeQuery = true)
    Advertise findAdvertiseForEasySell(@Param("memberId") Long memberId, @Param("coinId") Long coinId, @Param("money") BigDecimal money, @Param("payMode") String payMode, @Param("payMode2") String payMode2, @Param("payMode3") String payMode3);

    @Query(value = "select b.* from (" +
            "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.remain_amount >= :amount and a.status = 0 and a.advertise_type = 0 and  FIND_IN_SET(:payMode, a.pay_mode) " +
            " union all " +
            "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.remain_amount >= :amount and a.status = 0 and a.advertise_type = 0 and  FIND_IN_SET(:payMode2, a.pay_mode) " +
            " union all " +
            "select a.* from advertise a where a.member_id != :memberId and a.coin_id = :coinId and a.remain_amount >= :amount and a.status = 0 and a.advertise_type = 0 and  FIND_IN_SET(:payMode3, a.pay_mode) " +
            ") b order by b.price desc, b.top desc, b.create_time asc limit 1", nativeQuery = true)
    Advertise findAdvertiseForEasySellByAmount(@Param("memberId") Long memberId, @Param("coinId") Long coinId, @Param("amount") BigDecimal amount, @Param("payMode") String payMode, @Param("payMode2") String payMode2, @Param("payMode3") String payMode3);

    @Modifying
    @Query("update Advertise a set a.status=?1 where a.id=?2 and a.member.id=?3 and a.status<>?4")
    int updateAdvertiseStatus(AdvertiseControlStatus status, Long id, Long mid, AdvertiseControlStatus advertiseControlStatus);

    List<Advertise> findAllByMemberIdAndStatusAndAdvertiseType(Long var1, AdvertiseControlStatus status, AdvertiseType type);

    @Modifying
    @Query("update Advertise a set a.remainAmount=a.remainAmount-:amount,a.dealAmount=a.dealAmount+:amount where a.remainAmount>=:amount and a.status=:sta and a.id=:id")
    int updateAdvertiseAmount(@Param("sta") AdvertiseControlStatus status, @Param("id") Long id, @Param("amount") BigDecimal amount);


    @Modifying
    @Query("update Advertise a set a.dealAmount=a.dealAmount-:amount where a.dealAmount>=:amount  and a.id=:id")
    int updateAdvertiseDealAmount(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("update Advertise a set a.status=1,a.remainAmount=0,a.number=0 where a.remainAmount=:amount  and a.id=:id and a.status=0")
    int putOffAdvertise(@Param("id") Long id, @Param("amount") BigDecimal amount);

    List<Advertise> findAllByMemberIdAndStatus(Long var, AdvertiseControlStatus status);

    @Modifying
    @Query("update Advertise a set a.status = :status,a.updateTime=:updateTime where a.id in :ids")
    int alterStatusBatch(@Param("status") AdvertiseControlStatus status, @Param("updateTime") Date updateTime, @Param("ids") Long[] ids) ;


    @Query("select count(a.id) from Advertise a where a.member = :member")
    Long getAdvertiseNum(@Param("member") Member member);

    @Modifying
    @Query("update Advertise a set a.dealAmount=a.dealAmount-:amount,a.remainAmount=a.remainAmount+:amount where a.dealAmount>=:amount  and a.id=:id")
    int updateAdvertiseDealAmountAndRemainAmount(@Param("id") Long id, @Param("amount") BigDecimal amount);

    int countAllByMemberAndStatus(Member member, AdvertiseControlStatus status);

    @Modifying
    @Query("update Advertise a set a.top = :top,a.updateTime=:updateTime where a.id = :id")
    int alterTopBatch(@Param("top") Integer top, @Param("updateTime") Date updateTime, @Param("id") Long id) ;
}
