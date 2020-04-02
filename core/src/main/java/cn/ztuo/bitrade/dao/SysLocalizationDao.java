package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.SysLocalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * @author Zane
 * @version 1.0.0 2019-12-03
 */
public interface SysLocalizationDao extends JpaRepository<SysLocalization, String>, JpaSpecificationExecutor<SysLocalization>, QuerydslPredicateExecutor<SysLocalization> {
    @Modifying
    @Query("update SysLocalization s set s.content=?1 where s.id=?2 and s.locale=?3")
    int updateSysLocalization(String content, String id, String locale);

}
