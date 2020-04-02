package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Member;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MemberDao extends BaseDao<Member> {

    List<Member> getAllByEmailEquals(String email);

    List<Member> getAllByUsernameEquals(String username);

    List<Member> getAllByMobilePhoneEquals(String phone);

    Member findByUsername(String username);

    Member findMemberByTokenAndTokenExpireTimeAfter(String token, Date date);

    Member findMemberByTokenAndTokenExpireTimeAfterAndStatus(String token, Date date, CommonStatus status);

    Member findMemberByTokenWebAndTokenWebExpireTimeAfterAndStatus(String token, Date date, CommonStatus status);

    Member findMemberByToken(String token);

    Member findMemberByMobilePhoneOrEmail(String phone, String email);

    int countByRegistrationTimeBetween(Date startTime, Date endTime);

    Member findMemberByPromotionCode(String code);

    Member findMemberByEmail(String email);

    Member findMemberByMobilePhone(String mobilePhone);

    List<Member> findAllByInviterId(Long id);

    /*@Query("select new cn.ztuo.bitrade.dto.MemberDTO(member,memberWallet) from")*/

    @Query(value = "select m.username from member m where m.id = :id", nativeQuery = true)
    String findUserNameById(@Param("id") Long id);

    @Modifying
    @Query(value = "update Member set signInAbility = true ")
    void resetSignIn();


    @Query(value = "update Member set certified_business_status = :status where id in (:idList) and certified_business_status=2")
    void updateCertifiedBusinessStatusByIdList(@Param("idList") List<Long> idList, @Param("status") CertifiedBusinessStatus status);

    @Query(value = "select count(id) from member where date_format(registration_time,'%Y-%m-%d') = :date", nativeQuery = true)
    int getRegistrationNum(@Param("date") String date);

    @Query(value = "select count(id) from member where registration_time is not null ", nativeQuery = true)
    int getRegistrationSum();

    @Query(value = "select date_format(registration_time,'%Y-%m-%d') date_ ,count(id) count_ from member" +
            " where date_format(registration_time,'%Y-%m-%d') between ?1 and ?2  group by date_format(registration_time,'%Y-%m-%d')", nativeQuery = true)
    List<Map<String, String>> getRegistrationNumList(String startDate, String endDate);

    @Query(value = "select count(id) from member where date_format(certified_business_check_time,'%Y-%m-%d') = :date and certified_business_status = 2", nativeQuery = true)
    int getBusinessNum(@Param("date") String date);

    @Query(value = "select count(id) from member where certified_business_check_time is not null", nativeQuery = true)
    int getBusinessSum();


    @Query(value = "select date_format(certified_business_check_time,'%Y-%m-%d') date_ ,count(id) count_ from member" +
            " where date_format(certified_business_check_time,'%Y-%m-%d') between ?1 and ?2  group by date_format(certified_business_check_time,'%Y-%m-%d')", nativeQuery = true)
    List<Map<String, String>> getBusinessNumList(String startDate, String endDate);

    //以前没有application_time,若以此方法，需手动更新 在添加application_time字段之前的会员的实名通过时间
    /*
        update member a , member_application b
        set a.application_time = b.update_time
        where b.audit_status = 2 and a.application_time is NULL
        and a.id = b.member_id;
     */
    /*@Query(value ="select count(id) from member where date_format(application_time,'%Y-%m-%d') = :date",nativeQuery = true)
    int getApplicationNum(@Param("date")String date);*/

    @Query(value = "select count(a.id) from member a , member_application b where a.id = b.member_id and b.audit_status = 2 and date_format(b.update_time,'%Y-%m-%d') = :date", nativeQuery = true)
    int getApplicationNum(@Param("date") String date);

    @Query(value = "SELECT count(a.id) from member a, member_application b WHERE a.id = b.member_id AND b.audit_status = 2", nativeQuery = true)
    int getApplicationSum();

    @Query(value = "select date_format(b.update_time,'%Y-%m-%d') date_, count(a.id) count_ from member a , member_application b where a.id = b.member_id and b.audit_status = 2" +
            " and (date_format(b.update_time,'%Y-%m-%d') between ?1 and ?2) group by date_format(b.update_time,'%Y-%m-%d')", nativeQuery = true)
    List<Map<String, String>> getApplicationNumList(String startDate, String endDate);

    @Query("select min(a.registrationTime) as date from Member a")
    Date getStartRegistrationDate();

    @Modifying
    @Query(value = "update Member set channelId = :channelId where id = :memberId")
    int updateChannelId(@Param("memberId") Long memberId, @Param("channelId") Long channelId);

    @Query(value = "select m.channel_id as memberId,count(m.id) as channelCount,IFNULL((select sum(amount) from member_transaction where type=16 and member_id=m.id),0) as channelReward from member m where channel_id in (:memberIds) GROUP BY m.channel_id", nativeQuery = true)
    List<Object[]> getChannelCount(@Param("memberIds") List<Long> memberIds);

    @Modifying
    @Query("update Member m set m.loginLock= :loginLock where m.mobilePhone= :userName or m.email = :userName")
    int updateLoginLock(@Param("userName") String userName, @Param("loginLock") BooleanEnum loginLock);


    @Modifying
    @Query("update Member m set m.memberGradeId = :memberGradeId where m.id in (:ids) and m.memberGradeId <> :memberGradeId")
    int updateMemberGrades(@Param("ids") Collection<Long> ids, @Param("memberGradeId") Long memberGradeId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO member_wallet(coin_id,member_id,balance,version,frozen_balance) VALUES (:coinId,:memberId,:balance," +
            "0,0)", nativeQuery = true)
    Integer saveWallet(@Param("coinId") String coinId, @Param("memberId") Long memberId, @Param("balance") BigDecimal balance);

    @Query(value = "select id from member_wallet where member_id = :memberId for update", nativeQuery = true)
    List<Integer> selectMemberWalletForUpdate(@Param("memberId") Long memberId);
}
