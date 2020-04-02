package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, String>,
        JpaSpecificationExecutor<PaymentHistory>, QuerydslPredicateExecutor<PaymentHistory> {
    List<PaymentHistory> findByMemberIdAndLeverCoinAndCoin(Long memberId, LeverCoin leverCoin, Coin coin);

    PaymentHistory findById(Long id);
}
