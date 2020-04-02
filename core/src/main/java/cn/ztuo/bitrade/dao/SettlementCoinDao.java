package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.SettlementCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementCoinDao extends JpaRepository<SettlementCoin, String>, JpaSpecificationExecutor<SettlementCoin>, QuerydslPredicateExecutor<SettlementCoin> {
    @Query("select a.coinName from  SettlementCoin a where a.status = 0 order by a.sort")
    List<String> findBaseSymbol();
}
