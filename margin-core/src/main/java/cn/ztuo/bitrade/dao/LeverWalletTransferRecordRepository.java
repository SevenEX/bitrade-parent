package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.LeverWalletTransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface LeverWalletTransferRecordRepository  extends JpaRepository<LeverWalletTransferRecord, String>,
        JpaSpecificationExecutor<LeverWalletTransferRecord>, QuerydslPredicateExecutor<LeverWalletTransferRecord> {
    List<LeverWalletTransferRecord> findByMemberId(Long memberId);
    List<LeverWalletTransferRecord> findByMemberIdAndType(Long memberId, Integer type);
}
