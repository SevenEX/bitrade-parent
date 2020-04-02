package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.entity.LeverCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeverCoinRepository extends JpaRepository<LeverCoin, String>,
        JpaSpecificationExecutor<LeverCoin>, QuerydslPredicateExecutor<LeverCoin> {
    LeverCoin getBySymbol(String symbol);

    List<LeverCoin> findByEnable(BooleanEnum enable);

    /**
     * 查询所有的基币
     * @return
     */
    @Query("select distinct a.baseSymbol from  LeverCoin a where a.enable = 1")
    List<String> findBaseSymbol();

    @Query("select distinct a.coinSymbol from  LeverCoin a where a.enable = 1 and a.baseSymbol = :baseSymbol")
    List<String> findCoinSymbol(@Param("baseSymbol") String baseSymbol);

    LeverCoin findById(Long id);

    void deleteById(Long id);
//    @Query(value = "select * from lever_coin where symbol like  and enable=:enable",nativeQuery = true)
    List<LeverCoin> findLeverCoinsBySymbolLikeAndEnable(String coinUnit , BooleanEnum enable);
}
