package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.LocalizationExtend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * @author Zane
 * @version 1.0.0 2019-12-03
 */
public interface LocalizationExtendDao extends JpaRepository<LocalizationExtend, String>, JpaSpecificationExecutor<LocalizationExtend>, QuerydslPredicateExecutor<LocalizationExtend> {

}
