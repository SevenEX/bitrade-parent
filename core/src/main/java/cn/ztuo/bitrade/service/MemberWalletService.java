package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.MemberWalletRelationDao;
import cn.ztuo.bitrade.entity.MemberWallet;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.entity.QMemberWallet;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.CoinDao;
import cn.ztuo.bitrade.dao.MemberDepositDao;
import cn.ztuo.bitrade.dao.MemberWalletDao;
import cn.ztuo.bitrade.dto.MemberWalletDTO;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.Order;
import cn.ztuo.bitrade.exception.InformationExpiredException;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.BigDecimalUtils;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.vo.ImportXmlVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class MemberWalletService extends BaseService {
    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    private MemberWalletRelationDao memberWalletRelationDao;
    @Autowired
    private CoinDao coinDao;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private MemberDepositDao depositDao;
    @Autowired
    private LocaleMessageSourceService messageSource;

    public static final Integer limit=1000;

    public MemberWallet save(MemberWallet wallet) {
        return memberWalletDao.saveAndFlush(wallet);
    }

    /**
     * 获取钱包
     *
     * @param coin     otc币种
     * @param memberId
     * @return
     */
    public MemberWallet findByOtcCoinAndMemberId(OtcCoin coin, long memberId) {
        Coin coin1 = coinDao.findByUnit(coin.getUnit());
        return memberWalletDao.findByCoinAndMemberId(coin1, memberId);
    }

    /**
     * 钱包充值
     *
     * @param wallet
     * @param amount
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult recharge(MemberWallet wallet, BigDecimal amount) {
        if (wallet == null) {
            return new MessageResult(500, "wallet cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new MessageResult(500, "amount must large then 0");
        }
        int result = memberWalletDao.increaseBalance(wallet.getId(), amount);
        if (result > 0) {
            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(amount);
            transaction.setSymbol(wallet.getCoin().getUnit());
            transaction.setAddress(wallet.getAddress());
            transaction.setMemberId(wallet.getMemberId());
            transaction.setType(TransactionType.RECHARGE);
            transaction.setFee(BigDecimal.ZERO);
            transaction.setIsQuick(BooleanEnum.IS_FALSE);
            transactionService.save(transaction);
            //增加记录
            return new MessageResult(0, messageSource.getMessage("SUCCESS"));
        } else {
            return new MessageResult(500, messageSource.getMessage("FAIL"));
        }
    }

    /**
     * 钱包充值
     *
     * @param coin    币种名称
     * @param address 地址
     * @param amount  金额
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult recharge(Coin coin, Long memberId, String address, BigDecimal amount, String txid,BooleanEnum isQuick) {
        MemberWallet wallet = findByCoinAndMemberId(coin, memberId);
        if (wallet == null) {
            return new MessageResult(500, "wallet cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new MessageResult(500, "amount must large then 0");
        }
        MemberDeposit deposit = new MemberDeposit();
        deposit.setAddress(address);
        deposit.setAmount(amount);
        deposit.setMemberId(wallet.getMemberId());
        deposit.setTxid(txid);
        deposit.setUnit(wallet.getCoin().getUnit());
        depositDao.save(deposit);

        memberWalletDao.increaseBalance(wallet.getId(), amount);
        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(amount);
        transaction.setSymbol(wallet.getCoin().getUnit());
        transaction.setAddress(wallet.getAddress());
        transaction.setMemberId(wallet.getMemberId());
        transaction.setType(TransactionType.RECHARGE);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setTxid(txid);
        transaction.setIsQuick(isQuick);
        transactionService.save(transaction);
        MessageResult messageResult = new MessageResult(0,messageSource.getMessage("SUCCESS"));
        messageResult.setData(wallet.getMemberId());
        return messageResult;

    }


    /**
     * 根据币种和钱包地址获取钱包
     *
     * @param coin
     * @param address
     * @return
     */
    public MemberWallet findByCoinAndAddress(Coin coin, String address) {
        return memberWalletDao.findByCoinAndAddress(coin, address);
    }

    /**
     * 根据币种和用户ID获取钱包
     *
     * @param coin
     * @param member
     * @return
     */
    public MemberWallet findByCoinAndMember(Coin coin, Member member) {
        return memberWalletDao.findByCoinAndMemberId(coin, member.getId());
    }

    public MemberWallet findByCoinUnitAndMemberId(String coinUnit, Long memberId) {
        Coin coin = coinDao.findByUnit(coinUnit);
        return memberWalletDao.findByCoinAndMemberId(coin, memberId);
    }

    public MemberWallet findByCoinAndMemberId(Coin coin, Long memberId) {
        return memberWalletDao.findByCoinAndMemberId(coin, memberId);
    }

    /**
     * 根据用户查找所有钱包
     *
     * @param member
     * @return
     */
    public List<MemberWallet> findAllByMemberId(Member member) {
        return memberWalletDao.findAllByMemberId(member.getId());
    }

    public List<MemberWallet> findAllByMemberId(Long memberId) {
        return memberWalletDao.findAllByMemberId(memberId);
    }

    public List<MemberWallet> findAllByMemberIdAndCoin(String name,Long memberId) {
        return memberWalletDao.findAllByMemberIdAndCoin(name,memberId);
    }
    /**
     * 冻结钱包
     *
     * @param memberWallet
     * @param amount
     * @return
     */
    public MessageResult freezeBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.freezeBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    /**
     * 解冻钱包
     *
     * @param memberWallet
     * @param amount
     * @return
     */
    public MessageResult thawBalance(MemberWallet memberWallet, BigDecimal amount) {
        int ret = memberWalletDao.thawBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
        }
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
            MemberWallet customerWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
            //卖方付出手续费
            int is = memberWalletDao.decreaseFrozen(customerWallet.getId(), BigDecimalUtils.add(order.getNumber(),order.getCommission()));
            if (is > 0) {
                MemberWallet memberWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
                //买房得到完整的币
                int a = memberWalletDao.increaseBalance(memberWallet.getId(), order.getNumber());
                if (a <= 0) {
                    throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
                }
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else {
            MemberWallet customerWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getMemberId());
            //卖方付出手续费
            int is = memberWalletDao.decreaseFrozen(customerWallet.getId(), BigDecimalUtils.add(order.getNumber(), order.getCommission()));
            if (is > 0) {
                //买方得到完整数量
                MemberWallet memberWallet = findByOtcCoinAndMemberId(order.getCoin(), order.getCustomerId());
                int a = memberWalletDao.increaseBalance(memberWallet.getId(), order.getNumber());
                if (a <= 0) {
                    throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
                }
            } else {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        }

    }



    /* */

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
        MemberWallet customerWallet = findByOtcCoinAndMemberId(order.getCoin(), sellerId);
        //卖币者，买币者要处理的金额
        BigDecimal sellerAmount, buyerAmount;
        if (order.getMemberId() == sellerId) {
            //广告商卖币
            sellerAmount = BigDecimalUtils.add(order.getNumber(), order.getCommission());
            buyerAmount = order.getNumber();
        } else {
            //客户卖币
            sellerAmount = BigDecimalUtils.add(order.getNumber(), order.getCommission());
            buyerAmount = order.getNumber();
        }
        int is = memberWalletDao.decreaseFrozen(customerWallet.getId(), sellerAmount);
        if (is > 0) {
            MemberWallet memberWallet = findByOtcCoinAndMemberId(order.getCoin(), buyerId);
            int a = memberWalletDao.increaseBalance(memberWallet.getId(), buyerAmount);
            if (a <= 0) {
                throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
            }
        } else {
            throw new InformationExpiredException(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    public int deductBalance(MemberWallet memberWallet, BigDecimal amount) {
        return memberWalletDao.decreaseBalance(memberWallet.getId(), amount);
    }

    public List<MemberWallet> findAll() {
        return memberWalletDao.findAll();
    }

    public List<MemberWallet> findAllByCoin(Coin coin) {
        return memberWalletDao.findAllByCoin(coin);
    }

    public long pageCount(Coin coin){
        Criteria<MemberWallet> specification = new Criteria<MemberWallet>();
        specification.add(Restrictions.eq("coin", coin, true));
        return memberWalletDao.count(specification);
    }

    public Page<MemberWallet> pageByCoin(Coin coin,int pageNo,int pageSize){
        Sort orders = Sort.by(new Sort.Order(Sort.Direction.ASC, "id"));
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, orders);
        Criteria<MemberWallet> specification = new Criteria<MemberWallet>();
        specification.add(Restrictions.eq("coin", coin, true));
        return memberWalletDao.findAll(specification,pageRequest);
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
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getIsLock() == BooleanEnum.IS_FALSE) {
            wallet.setIsLock(BooleanEnum.IS_TRUE);
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
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getIsLock() == BooleanEnum.IS_TRUE) {
            wallet.setIsLock(BooleanEnum.IS_FALSE);
            return true;
        } else {
            return false;
        }
    }

    public MemberWallet findOneByCoinNameAndMemberId(String coinName, long memberId) {
        BooleanExpression and = QMemberWallet.memberWallet.coin.name.eq(coinName)
                .and(QMemberWallet.memberWallet.memberId.eq(memberId));
        return memberWalletDao.findOne(and).orElse(null);
    }

    public Page<MemberWalletDTO> joinFind(List<Predicate> predicates, QMember qMember , QMemberWallet qMemberWallet, PageModel pageModel) {
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        predicates.add(qMember.id.eq(qMemberWallet.memberId));
        JPAQuery<MemberWalletDTO> query = queryFactory.select(
                        Projections.fields(MemberWalletDTO.class, qMemberWallet.id.as("id"),qMemberWallet.memberId.as("memberId") ,qMember.username,qMember.realName.as("realName"),
                        qMember.email,qMember.mobilePhone.as("mobilePhone"),qMemberWallet.balance,qMemberWallet.address,qMemberWallet.coin.unit
                        ,qMemberWallet.frozenBalance.as("frozenBalance"),qMemberWallet.balance.add(qMemberWallet.frozenBalance).as("allBalance"))).from(QMember.member,QMemberWallet.memberWallet).where(predicates.toArray(new Predicate[predicates.size()]))
                        .orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        List<MemberWalletDTO> content = query.offset((pageModel.getPageNo()-1)*pageModel.getPageSize()).limit(pageModel.getPageSize()).fetch();
        long total = query.fetchCount();
        return new PageImpl<>(content, pageModel.getPageable(), total);
    }

    public BigDecimal getAllBalance(String coinName){
        return memberWalletDao.getWalletAllBalance(coinName);
    }

    public MemberDeposit findDeposit(String address,String txid){
        return depositDao.findByAddressAndTxid(address,txid);
    }

    public MessageResult decreaseFrozen(Long walletId,BigDecimal amount){
        int ret = memberWalletDao.decreaseFrozen(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    public void decreaseBalance(Long walletId,BigDecimal amount){
        memberWalletDao.decreaseBalance(walletId,amount);
    }

    public void increaseBalance(Long walletId,BigDecimal amount){
        memberWalletDao.increaseBalance(walletId,amount);
    }


    /**
     * 创建快照表
     * @param times
     * @param coinId
     * @return
     */
    public Integer createGiftTable(Long times,String coinId){
        return memberWalletDao.createGiftTable(times,coinId);
    }


    public List<MemberWallet> findGiftTable(Long times){
        return memberWalletDao.findGiftTable(times);
    }

    public BigDecimal sumGiftTable(Long times){
        return memberWalletDao.sumGiftTable(times);

    }
    /**
     * 手动上传文件空投
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MessageResult handleAirdrop(List<ImportXmlVO> xmlVOS,Long airdrop){
        List<MemberTransaction> memberTransactionList=new ArrayList<>();
        List<Long> memberIdList=new ArrayList<>();
        for(ImportXmlVO xmlVO:xmlVOS){
            memberIdList.add(xmlVO.getMemberId());
        }
        //根据币种和memberId查询钱包，要求一个上传文件里都是空投同一个币种
        Coin coin=coinDao.findByUnit(xmlVOS.get(0).getCoinUnit());
        List<MemberWallet> memberWalletList=memberWalletDao.findALLByCoinIdAndMemberIdList(coin,memberIdList);
        if(memberWalletList==null||memberWalletList.size()==0||memberWalletList.size()!=xmlVOS.size()){
            return MessageResult.error("钱包不存在或钱包数量不足");
        }
        //录入memberTransaction表，新增一个手动空投类型
        for(ImportXmlVO xmlVO:xmlVOS){
            for(MemberWallet memberWallet:memberWalletList){
                if(xmlVO.getMemberId().equals(memberWallet.getMemberId())){
                    memberWalletDao.increaseBalance(memberWallet.getId(), new BigDecimal(xmlVO.getAmount()));
                }
            }
            MemberTransaction transaction = new MemberTransaction();
            transaction.setAmount(new BigDecimal(xmlVO.getAmount()));
            transaction.setSymbol(xmlVO.getCoinUnit());
            transaction.setAddress("");
            transaction.setMemberId(xmlVO.getMemberId());
            transaction.setType(TransactionType.MANUAL_AIRDROP);
            transaction.setFee(BigDecimal.ZERO);
            transaction.setAirdropId(airdrop);
            memberTransactionList.add(transaction);
        }
        transactionService.save(memberTransactionList);
        memberWalletDao.saveAll(memberWalletList);
        return MessageResult.success();
    }

    public MemberWalletRelation findWalletRelationByCoinKeyAndAddress(String coinKey, String address) {
        return memberWalletRelationDao.findByCoinKeyAndAddress(coinKey, address);
    }

    public void saveMemberWalletRelation(MemberWalletRelation memberWalletRelation) {
        memberWalletRelationDao.save(memberWalletRelation);
    }

    public Integer findWalletForUpdate(Long memberId, Coin coin) {
        return memberWalletDao.findWalletForUpdate(memberId, coin.getName());
    }
}
