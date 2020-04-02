package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.LeverWallet;
import cn.ztuo.bitrade.enums.WalletEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/25
 */
public interface LeverWalletRepository extends JpaRepository<LeverWallet, String>,
        JpaSpecificationExecutor<LeverWallet>, QuerydslPredicateExecutor<LeverWallet> {
    LeverWallet findByMemberIdAndLeverCoinAndCoinAndIsLock(Long memberId, LeverCoin leverCoin, Coin coin, BooleanEnum isLock);
    List<LeverWallet> findByMemberId(Long memberId);
    List<LeverWallet> findByMemberIdAndLeverCoin(Long memberId, LeverCoin leverCoin);
    List<LeverWallet> findByMemberIdAndIsLock(Long memberId, BooleanEnum isLock);
    List<LeverWallet> findByMemberIdAndLeverCoinAndIsLock(Long memberId, LeverCoin leverCoin, BooleanEnum isLock);
    List<LeverWallet> findByIsLock(BooleanEnum isLock);
    LeverWallet findByMemberIdAndCoinAndLeverCoin(Long memberId, Coin coin, LeverCoin leverCoin);
    List<LeverWallet> findByStatus(WalletEnum walletEnum);

    @Transactional
    @Modifying
    @Query("update LeverWallet wallet set wallet.isLock = :isLock,wallet.status = :status where wallet.memberId = :memberId")
    void updateLeverWalletByMemberId(@Param("memberId") Long memberId, @Param("isLock") BooleanEnum isLock, @Param("status") WalletEnum status);

    @Query(value="select w.memberId,w.leverCoin from LeverWallet w GROUP BY w.memberId,w.leverCoin",
        countQuery = "select count(w.memberId) from LeverWallet w GROUP BY w.memberId,w.leverCoin")
    Page<Object[]> listMarginMember(Pageable pageable);

    /**
     * 解冻钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Transactional
    @Modifying
    @Query("update LeverWallet wallet set wallet.balance = wallet.balance + :amount,wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int thawBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    LeverWallet findByMemberIdAndLeverCoinAndCoin(Long memberId, LeverCoin leverCoin, Coin coin);


    LeverWallet findById(Long id);
}
