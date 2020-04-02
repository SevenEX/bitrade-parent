package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.SysHelpClassification;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.SysHelp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author MrGao
 * @description
 * @date 2018/1/9 9:58
 */
public interface SysHelpDao extends BaseDao<SysHelp> {
    List<SysHelp> findAllBySysHelpClassificationAndStatusNotAndLocale(SysHelpClassification sysHelpClassification, CommonStatus commonStatus, String locale);

    @Query("select max(s.sort) from SysHelp s")
    int findMaxSort();

    @Query(value = "select * from sys_help WHERE sys_help_classification=:cate and is_top='0' and locale = :locale",nativeQuery = true)
    List<SysHelp> getCateTop(@Param("cate") String cate,@Param("locale") String locale);

    @Query(value = "SELECT * FROM sys_help s WHERE s.status ='0' and s.locale = :locale ORDER BY s.is_top asc , s.sort DESC", nativeQuery = true)
    List<SysHelp> findAllByStatusNotAndSortAndLocale(@Param("locale") String locale);
}
