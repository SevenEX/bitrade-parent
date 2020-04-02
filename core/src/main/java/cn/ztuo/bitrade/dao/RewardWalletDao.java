package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.RewardWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RewardWalletDao extends JpaRepository<RewardWallet, String>, JpaSpecificationExecutor<RewardWallet>, QuerydslPredicateExecutor<RewardWallet> {
    /**
     * 根据用户查询奖励钱包
     * @param memberId
     * @return
     */
    RewardWallet findRewardWalletByMemberId(Long memberId);

    /**
     * 根据用户查询奖励钱包
     * @param memberId
     *@param unit
     * @return
     */
    RewardWallet findRewardWalletByMemberIdAndCoinUnit(Long memberId, String unit);

    /**
     * 生成钱包
     * @param rewardWallet
     * @return
     */
    RewardWallet save(RewardWallet rewardWallet);

    /**
     * 增加钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount where wallet.id = :walletId")
    int increaseBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 减少钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance - :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int decreaseBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);

    /**
     * 根据用户查询奖励钱包
     * @param memberId
     * @return
     */
    List<RewardWallet> findAllByMemberId(Long memberId);

    /**
     * 根据用户查询奖励钱包
     * @param
     * @return
     */
    List<RewardWallet> findAll();
}
