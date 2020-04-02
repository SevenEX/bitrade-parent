package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.dto.CoinDTO;
import cn.ztuo.bitrade.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author MrGao
 * @description 货币操作
 * @date 2017/12/29 14:41
 */
@Repository
public interface CoinDao extends JpaRepository<Coin, String>, JpaSpecificationExecutor<Coin>, QuerydslPredicateExecutor<Coin> {
    Coin findByUnit(String unit);

    List<Coin> findAllByCanWithdrawAndStatusAndHasLegal(BooleanEnum is, CommonStatus status, boolean hasLegal);

    Coin findCoinByIsPlatformCoin(BooleanEnum is);

    List<Coin> findByHasLegal(Boolean hasLegal);

    @Query("select a from Coin a where a.unit in (:units) ")
    List<Coin> findAllByOtc(@Param("units") List<String> otcUnits);

    @Query("select a.name from Coin a")
    List<String> findAllName();

    @Query(value = "select  new cn.ztuo.bitrade.dto.CoinDTO(a.name,a.unit) from Coin a")
    List<CoinDTO> findAllNameAndUnit();

    @Query("select a.name from Coin a where a.hasLegal = true ")
    List<String> findAllCoinNameLegal();

    @Query("select a.unit from Coin a where a.enableRpc = 1")
    List<String> findAllRpcUnit();

    List<Coin> findAllByIsPlatformCoin(BooleanEnum isPlatformCoin);

    /**
     * 查询指定币种的总额
     * @param coin
     * @return
     */
    @Query("SELECT sum(a.balance) FROM MemberWallet a WHERE a.coin = :coin")
    BigDecimal sumBalance(@Param("coin") Coin coin);

    /**
     * 根据用户ID查询指定币种的总额
     * @param coin
     * @return
     */
    @Query("SELECT a.balance FROM MemberWallet a WHERE a.coin = :coin AND a.memberId = :memberId")
    BigDecimal getBalanceByMemberIdAndCoinId(@Param("coin") Coin coin, @Param("memberId") Long memberId);

    @Query("select a from Coin a order by a.sort")
    List<Coin> findAllOrderBySort();

    List<Coin> findAllByStatus(CommonStatus status);

    List<Coin> findByStatus(CommonStatus status);

    List<Coin> findAllByStatusAndIsSettlement(CommonStatus status, boolean isSettlement);

    List<Coin> findAllByStatusAndIsSettlementNot(CommonStatus status, boolean isSettlement);

}
