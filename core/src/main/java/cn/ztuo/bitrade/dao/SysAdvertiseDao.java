package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.SysAdvertiseLocation;
import cn.ztuo.bitrade.entity.SysAdvertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

/**
 * @author MrGao
 * @description
 * @date 2018/1/6 16:44
 */
public interface SysAdvertiseDao extends JpaRepository<SysAdvertise, String>, JpaSpecificationExecutor<SysAdvertise>, QuerydslPredicateExecutor<SysAdvertise> {
    List<SysAdvertise> findAllByStatusAndSysAdvertiseLocationAndLocaleOrderBySortDesc(CommonStatus status, SysAdvertiseLocation sysAdvertiseLocation, String locale);

    @Query("select max(s.sort) from SysAdvertise s")
    int findMaxSort();

}
