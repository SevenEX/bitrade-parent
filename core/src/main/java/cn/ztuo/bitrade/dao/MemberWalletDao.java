package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.MemberWallet;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface MemberWalletDao extends BaseDao<MemberWallet> {

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
     * 解冻钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance + :amount,wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int thawBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    /**
     * 冻结钱包余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance = wallet.balance - :amount,wallet.frozenBalance=wallet.frozenBalance + :amount where wallet.id = :walletId and wallet.balance >= :amount")
    int freezeBalance(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    /**
     * 减少冻结余额
     *
     * @param walletId
     * @param amount
     * @return
     */
    @Modifying
    @Query("update MemberWallet wallet set wallet.frozenBalance=wallet.frozenBalance - :amount where wallet.id = :walletId and wallet.frozenBalance >= :amount")
    int decreaseFrozen(@Param("walletId") long walletId, @Param("amount") BigDecimal amount);


    MemberWallet findByCoinAndAddress(Coin coin, String address);

    MemberWallet findByCoinAndMemberId(Coin coin, Long memberId);

    List<MemberWallet> findAllByMemberId(Long memberId);

    @Query(value = "select * from member_wallet  where coin_id like CONCAT('%',:coinId,'%') and member_id=:memberId",nativeQuery = true)
    List<MemberWallet> findAllByMemberIdAndCoin(@Param("coinId")String coinId,@Param("memberId")Long memberId);

    List<MemberWallet> findAllByCoin(Coin coin);

    @Query(value="select ifnull(sum(a.balance),0)+ ifnull(sum(a.frozen_balance),0) as allBalance from member_wallet a where a.coin_id = :coinName",nativeQuery = true)
    BigDecimal getWalletAllBalance(@Param("coinName") String coinName);


    @Query("select m from MemberWallet m where m.coin=:coin and m.memberId in (:memberIdList)")
    List<MemberWallet> findALLByCoinIdAndMemberIdList(@Param("coin") Coin coin, @Param("memberIdList") List<Long> memberIdList);

    @Query(value = "select * from member_wallet  where coin_id=:coinId and member_id=:memberId",nativeQuery = true)
    MemberWallet findCoinIdAndMemberId(@Param("coinId")String coinId,@Param("memberId")Long memberId);

    /**
     * 新增账户快照表
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "CREATE TABLE member_wallet_:times SELECT * FROM member_wallet where coin_id=:coinId AND balance>0" +
            " AND is_lock=0",nativeQuery = true)
    Integer createGiftTable(@Param("times")Long times,@Param("coinId")String coinId);

    @Query(value = "SELECT * FROM member_wallet_:times ",nativeQuery = true)
    List<MemberWallet> findGiftTable(@Param("times")Long times);

    @Query(value = "SELECT sum(balance) FROM member_wallet_:times ",nativeQuery = true)
    BigDecimal sumGiftTable(@Param("times")Long times);

    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("update MemberWallet wallet set wallet.balance=wallet.balance + :amount,wallet.releaseBalance= wallet.releaseBalance -:amount where wallet.id = :walletId and wallet.releaseBalance >= :amount")
    int releaseReisterGiving(@Param("amount") BigDecimal amount, @Param("walletId") Long id);

    @Query(value = "select id from member_wallet where member_id = :memberId and coin_id = :coinId for update", nativeQuery = true)
    Integer findWalletForUpdate(@Param("memberId")Long memberId, @Param("coinId")String coinId);
}
