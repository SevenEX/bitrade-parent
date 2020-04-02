package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.AssetExchangeCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AssetExchangeDao extends JpaRepository<AssetExchangeCoin,Long>,JpaSpecificationExecutor<AssetExchangeCoin> {
    List<AssetExchangeCoin> findAllByToUnit(String unit);

    AssetExchangeCoin findByFromUnitAndToUnit(String fromUnit, String toUnit);
}
