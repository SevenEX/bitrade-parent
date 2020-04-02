package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.CoinDao;
import cn.ztuo.bitrade.dao.OtcWalletDao;
import cn.ztuo.bitrade.dto.OtcWalletDTO;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static cn.ztuo.bitrade.util.BigDecimalUtils.sub;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/5/5 3:26 PM
 */
@Service
public class OtcWalletService extends BaseService {

    @Autowired
    private OtcWalletDao otcWalletDao;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private CoinDao coinDao;


    public List<OtcWallet> findByMemberId(Long memberId) {
        return otcWalletDao.findOtcWalletByMemberId(memberId);
    }


    /**
     * 获取钱包
     *
     * @param coin     otc币种
     * @param memberId
     * @return
     */
    public OtcWallet findByOtcCoinAndMemberId(Long memberId,OtcCoin coin) {
        Coin coin1 = coinDao.findByUnit(coin.getUnit());
        return otcWalletDao.findByMemberIdAndCoin(memberId,coin1);
    }

    public OtcWallet findByCoinAndMember(Long memberId,Coin coin) {
        return otcWalletDao.findByMemberIdAndCoin(memberId,coin);
    }

    public OtcWallet findByCoinUnitAndMemberId(Long memberId,String coinUnit) {
        Coin coin = coinDao.findByUnit(coinUnit);
        return otcWalletDao.findByMemberIdAndCoin(memberId,coin);
    }

    public OtcWallet save(OtcWallet otcWallet){
        return otcWalletDao.save(otcWallet);
    }

    public Integer addWallet(Long id,BigDecimal amount){
        return otcWalletDao.addWallet(id,amount);
    }
    /**
     * 币币转法币
     * @param memberWallet
     * @param otcWallet
     * @param amount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer coin2Otc(MemberWallet memberWallet, OtcWallet otcWallet, BigDecimal amount){
        //扣减币币账户
        memberWalletService.decreaseBalance(memberWallet.getId(),amount);

        //增加法币账户
        Integer addResult = otcWalletDao.addWallet(otcWallet.getId(),amount);
        if (addResult == 0){
            return 0;
        }
        //增加划转记录
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(amount);
        transaction.setSymbol(memberWallet.getCoin().getName());
        transaction.setAddress("");
        transaction.setMemberId(memberWallet.getMemberId());
        transaction.setType(TransactionType.COIN_TWO_OTC);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setCreateTime(new Date());
        transactionService.save(transaction);
        return 1;
    }

    /**
     * 法币转币币
     * @param memberWallet
     * @param otcWallet
     * @param amount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer otc2Coin(MemberWallet memberWallet, OtcWallet otcWallet, BigDecimal amount){
        //扣减法币账户
        Integer subResult = otcWalletDao.subWallet(otcWallet.getId(),amount);
        if (subResult == 0){
            return 0;
        }
        //增加币币账户
        memberWalletService.increaseBalance(memberWallet.getId(),amount);

        //增加划转记录
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(amount);
        transaction.setSymbol(memberWallet.getCoin().getName());
        transaction.setAddress("");
        transaction.setMemberId(memberWallet.getMemberId());
        transaction.setType(TransactionType.OTC_TWO_COIN);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setCreateTime(new Date());
        transactionService.save(transaction);
        return 1;

    }

    /**
     * 解冻钱包
     *
     * @param otcWallet
     * @param amount
     * @return
     */
    public MessageResult thawBalance(OtcWallet otcWallet, BigDecimal amount) {
        int ret = otcWalletDao.thawBalance(otcWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    /**
     * 冻结减少
     * @param walletId
     * @param amount
     */
    public void decreaseFrozen(Long walletId,BigDecimal amount){
        otcWalletDao.decreaseFrozen(walletId,amount);
    }


    /**
     * 放行更改双方钱包余额
     *
     * @param order
     * @param ret
     * @throws InformationExpiredException
     */
    public void transfer(Order order, int ret) throws InformationExpiredException {
        if (ret == 1) {
            OtcWallet customerWallet = findByOtcCoinAndMemberId( order.getCustomerId(),order.getCoin());
            //卖方解除订单金额的冻结
            int is = otcWalletDao.decreaseFrozen(customerWallet.getId(), order.getNumber());
            if (is > 0) {
                OtcWallet memberWallet = findByOtcCoinAndMemberId(order.getMemberId(),order.getCoin());
                //买方得到的币扣除手续费
                int a = otcWalletDao.increaseBalance(memberWallet.getId(), sub(order.getNumber(), order.getCommission()));
                if (a <= 0) {
                    throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
                }
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else {
            OtcWallet customerWallet = findByOtcCoinAndMemberId( order.getMemberId(),order.getCoin());
            //卖方付出手续费
            int is = otcWalletDao.decreaseFrozen(customerWallet.getId(), BigDecimalUtils.add(order.getNumber(), order.getCommission()));
            if (is > 0) {
                //买方得到完整数量
                OtcWallet memberWallet = findByOtcCoinAndMemberId( order.getCustomerId(),order.getCoin());
                int a = otcWalletDao.increaseBalance(memberWallet.getId(), order.getNumber());
                if (a <= 0) {
                    throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
                }
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }

    }

    /**
     * 冻结钱包
     *
     * @param otcWallet
     * @param amount
     * @return
     */
    public MessageResult freezeBalance(OtcWallet otcWallet, BigDecimal amount) {
        int ret = otcWalletDao.freezeBalance(otcWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    public void increaseBalance(Long walletId,BigDecimal amount){
        otcWalletDao.increaseBalance(walletId,amount);
    }


    /**
     * 放行更改双方钱包余额
     *
     * @param order
     * @param ret
     * @throws InformationExpiredException
     */
    public void transferAdmin(Order order, int ret) throws InformationExpiredException {
        if (ret == 1 || ret == 4) {
            trancerDetail(order, order.getCustomerId(), order.getMemberId());
        } else {
            trancerDetail(order, order.getMemberId(), order.getCustomerId());

        }

    }


    private void trancerDetail(Order order, long sellerId, long buyerId) throws InformationExpiredException {
        OtcWallet customerWallet = findByOtcCoinAndMemberId(sellerId,order.getCoin());
        //卖币者，买币者要处理的金额
        BigDecimal sellerAmount, buyerAmount;
        if (order.getMemberId() == sellerId) {
            //广告商卖币
            sellerAmount = BigDecimalUtils.add(order.getNumber(), order.getCommission());
            buyerAmount = order.getNumber();
        } else {
            //客户卖币
            sellerAmount = order.getNumber();
            buyerAmount = BigDecimalUtils.sub(order.getNumber(), order.getCommission());
        }
        int is = otcWalletDao.decreaseFrozen(customerWallet.getId(), sellerAmount);
        if (is > 0) {
            OtcWallet memberWallet = findByOtcCoinAndMemberId(buyerId,order.getCoin());
            int a = otcWalletDao.increaseBalance(memberWallet.getId(), buyerAmount);
            if (a <= 0) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    /**
     * 锁定钱包
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean lockWallet(Long uid, String unit) {
        OtcWallet wallet = findByCoinUnitAndMemberId(uid,unit);
        if (wallet != null && wallet.getIsLock() == 0) {
            wallet.setIsLock(1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 解锁钱包
     *
     * @param uid
     * @param unit
     * @return
     */
    @Transactional
    public boolean unlockWallet(Long uid, String unit) {
        OtcWallet wallet = findByCoinUnitAndMemberId(uid,unit);
        if (wallet != null && wallet.getIsLock() == 1) {
            wallet.setIsLock(0);
            return true;
        } else {
            return false;
        }
    }


    public Page<OtcWalletDTO> joinFind(List<Predicate> predicates, QMember qMember , QOtcWallet qMemberWallet, PageModel
            pageModel) {
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        predicates.add(qMember.id.eq(qMemberWallet.memberId));
        JPAQuery<OtcWalletDTO> query = queryFactory.select(
                Projections.fields(OtcWalletDTO.class, qMemberWallet.id.as("id"),qMemberWallet.memberId.as("memberId") ,qMember.username,qMember.realName.as("realName"),
                        qMember.email,qMember.mobilePhone.as("mobilePhone"),qMemberWallet.balance,qMemberWallet.coin.unit
                        ,qMemberWallet.frozenBalance.as("frozenBalance"),qMemberWallet.balance.add(qMemberWallet
                                .frozenBalance).as("allBalance"))).from(QMember.member,QOtcWallet.otcWallet).where
                (predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        List<OtcWalletDTO> content = query.offset((pageModel.getPageNo()-1)*pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total = query.fetchCount();
        return new PageImpl<>(content, pageModel.getPageable(), total);
    }

}