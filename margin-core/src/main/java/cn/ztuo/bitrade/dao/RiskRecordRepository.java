package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.RiskRecord;
import cn.ztuo.bitrade.enums.PerformActionsEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface RiskRecordRepository extends JpaRepository<RiskRecord, String>,
        JpaSpecificationExecutor<RiskRecord>, QuerydslPredicateExecutor<RiskRecord> {
    List<RiskRecord> findByMemberId(Long memberId);
    List<RiskRecord> findByMemberIdAndLeverCoin(Long memberId, LeverCoin leverCoin);
    List<RiskRecord> findByPerformActionsAndMemberId(PerformActionsEnum performActions, Long memberId);
}
