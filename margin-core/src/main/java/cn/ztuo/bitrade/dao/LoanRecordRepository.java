package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.LoanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/25
 */
public interface LoanRecordRepository extends JpaRepository<LoanRecord, String>,
        JpaSpecificationExecutor<LoanRecord>, QuerydslPredicateExecutor<LoanRecord> {
    List<LoanRecord> findByMemberIdAndLeverCoinAndRepayment(Long memberId, LeverCoin leverCoin, BooleanEnum repayment);
    List<LoanRecord> findByMemberIdAndLeverCoinAndCoinAndRepayment(Long memberId, LeverCoin leverCoin, Coin coin, BooleanEnum repayment);
    List<LoanRecord> findAllByRepayment(BooleanEnum repayment);
    LoanRecord findById(Long id);
    List<LoanRecord> findByMemberIdAndLeverCoinOrderByRepayment(Long memberId, LeverCoin leverCoin);
    List<LoanRecord> findByMemberIdAndLeverCoin(Long memberId, LeverCoin leverCoin);
}
