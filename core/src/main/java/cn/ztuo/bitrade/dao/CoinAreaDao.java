package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.CoinArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MrGao
 * @description 货币操作
 * @date 2017/12/29 14:41
 */
@Repository
public interface CoinAreaDao extends JpaRepository<CoinArea, Long>, JpaSpecificationExecutor<CoinArea>, QuerydslPredicateExecutor<CoinArea> {

    @Query("select a from CoinArea a where a.status = '0' order by a.sort desc ")
    List<CoinArea> findAll();

    void deleteById(Long id);

}
