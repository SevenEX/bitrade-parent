package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.FavorSymbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavorSymbolRepository extends JpaRepository<FavorSymbol,Long>{
    FavorSymbol findByMemberIdAndSymbol(Long memberId, String symbol);
    List<FavorSymbol> findAllByMemberId(Long memberId);
    @Query(value = "select favorSymbol from FavorSymbol favorSymbol where favorSymbol.memberId = :memberId and favorSymbol.symbol like  CONCAT('%',:symbol,'%')")
    List<FavorSymbol> findByMemberIdAndCoinName(@Param("memberId") Long memberId, @Param("symbol") String symbol);
}
