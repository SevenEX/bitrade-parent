package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.MemberDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberDepositDao extends JpaRepository<MemberDeposit,Long>,QuerydslPredicateExecutor<MemberDeposit>{
    MemberDeposit findByAddressAndTxid(String address, String txid);

    @Query(value="select unit ,sum(amount) from member_deposit where date_format(create_time,'%Y-%m-%d') = :date group by unit",nativeQuery = true)
    List<Object[]> getDepositStatistics(@Param("date") String date);

}
