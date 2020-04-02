package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

/**
 * @author Seven
 * @date 2019年02月10日
 */
public interface CountryDao extends JpaRepository<Country,String>,JpaSpecificationExecutor<Country>,QuerydslPredicateExecutor<Country> {
    @Query("select a from Country a order by a.sort")
    List<Country> findAllOrderBySort();

    Country findByZhName(String zhname);

    List<Country> findByLocalCurrency(String localCurrency);
}
