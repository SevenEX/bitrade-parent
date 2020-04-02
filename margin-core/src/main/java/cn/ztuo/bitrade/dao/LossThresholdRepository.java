package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.LossThreshold;
import cn.ztuo.bitrade.enums.PerformActionsEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

public interface LossThresholdRepository extends JpaRepository<LossThreshold, String>,
        JpaSpecificationExecutor<LossThreshold>, QuerydslPredicateExecutor<LossThreshold> {
    List<LossThreshold> findByLeverCoin(LeverCoin leverCoin);

    List<LossThreshold> findAll();

    LossThreshold findByLeverCoinAndThreshold(LeverCoin leverCoin, BigDecimal threshold);

    LossThreshold findById(Long id);

    List<LossThreshold> findAllByStatusOrderByThresholdDesc(CommonStatus status);

    @Transactional
    void deleteById(Long id);

    LossThreshold findByLeverCoinAndPerformActions(LeverCoin leverCoin, PerformActionsEnum performActions);
}
