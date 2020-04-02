package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.dao.CoinDao;
import cn.ztuo.bitrade.dao.LeverWalletRepository;
import cn.ztuo.bitrade.dao.LeverWalletTransferRecordRepository;
import cn.ztuo.bitrade.dao.MemberWalletDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.enums.WalletEnum;
import cn.ztuo.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhang yingxin
 * @date 2018/5/25
 */
@Service
public class LeverWalletService {
    @Autowired
    private LeverWalletRepository leverWalletRepository;
    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    private CoinDao coinDao;
    @Autowired
    private LeverWalletTransferRecordRepository leverWalletTransferRecordRepository;
    @Autowired
    private LocaleMessageSourceService msService;
    
    public LeverWallet findByMemberIdAndLeverCoinAndCoinAndIsLock(Long memberId, LeverCoin leverCoin, Coin coin, BooleanEnum isLock){
        return leverWalletRepository.findByMemberIdAndLeverCoinAndCoinAndIsLock(memberId,leverCoin,coin,isLock);
    }

    public LeverWallet findByMemberIdAndLeverCoinAndCoin(Long memberId, LeverCoin leverCoin, Coin coin, BooleanEnum isLock){
        return leverWalletRepository.findByMemberIdAndLeverCoinAndCoin(memberId,leverCoin,coin);
    }

    public List<LeverWallet> findByMemberId(Long memberId){
        return leverWalletRepository.findByMemberId(memberId);
    }

    public List<LeverWallet> findByMemberIdAndLeverCoin(Long memberId,LeverCoin leverCoin){
        return leverWalletRepository.findByMemberIdAndLeverCoin(memberId,leverCoin);
    }

    public List<LeverWallet> findByMemberIdAndIsLock(Long memberId,BooleanEnum isLock){
        return leverWalletRepository.findByMemberIdAndIsLock(memberId,isLock);
    }

    public List<LeverWallet> findByMemberIdAndLeverCoinAndIsLock(Long memberId,LeverCoin leverCoin,BooleanEnum isLock){
        return leverWalletRepository.findByMemberIdAndLeverCoinAndIsLock(memberId,leverCoin,isLock);
    }

    public List<LeverWallet> findAll(){
        return leverWalletRepository.findAll();
    }

    public Page<LeverWallet> findAll(Predicate predicate, Pageable pageable){
        return leverWalletRepository.findAll(predicate,pageable);
    }

    public List<LeverWallet> findByIsLock(BooleanEnum isLock){
        return leverWalletRepository.findByIsLock(isLock);
    }

    public void create(LeverWallet leverWallet){
        leverWalletRepository.save(leverWallet);
    }

    public void updateLeverWalletByMemberId(Long memberId,BooleanEnum isLock,WalletEnum status){
        leverWalletRepository.updateLeverWalletByMemberId(memberId,isLock,status);
    }

    public LeverWallet findByMemberIdAndCoinAndLeverCoin(Long memberId,Coin coin,LeverCoin leverCoin){
        return leverWalletRepository.findByMemberIdAndCoinAndLeverCoin(memberId,coin,leverCoin);
    }

    public void save(LeverWallet leverWallet){
        leverWalletRepository.save(leverWallet);
    }

    public List<LeverWallet> findByStatus(WalletEnum status){
        return leverWalletRepository.findByStatus(status);
    }

    /**
     * 普通钱包转钱到杠杆钱包
     * @param member
     * @param amount
     * @param coin
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean transferInto(Member member, BigDecimal amount,Coin coin,LeverCoin leverCoin){
        MemberWallet memberWallet=memberWalletDao.findByCoinAndMemberId(coin,member.getId());
        if(memberWallet==null||memberWallet.getBalance().compareTo(amount)<0){
            return false;
        }
        LeverWallet leverWallet=leverWalletRepository.findByMemberIdAndLeverCoinAndCoin(member.getId(),leverCoin,coin);
        //普通钱包金额减少
        memberWalletDao.decreaseBalance(memberWallet.getId(), amount);
        //如果杠杆钱包不存在，创建并转入钱
        if(leverWallet==null){
            //先生成基准币
            if(leverCoin.getBaseSymbol().equalsIgnoreCase(coin.getUnit())) {
                saveLeverWallet(member,coin,amount,leverCoin);
                String coinUnit = leverCoin.getCoinSymbol() ;
                Coin otherCoin = coinDao.findByUnit(coinUnit);
                saveLeverWallet(member,otherCoin,BigDecimal.ZERO,leverCoin);
            }else {
                String coinUnit = leverCoin.getBaseSymbol() ;
                Coin otherCoin = coinDao.findByUnit(coinUnit);
                saveLeverWallet(member,otherCoin,BigDecimal.ZERO,leverCoin);
                saveLeverWallet(member,coin,amount,leverCoin);
            }
            //创建这个币对的另一种币的杠杆钱包

        }else if(leverWallet.getIsLock().equals(BooleanEnum.IS_TRUE)){
            return false;
        }else{
            leverWallet.setBalance(leverWallet.getBalance().add(amount));
        }

        LeverWalletTransferRecord record=new LeverWalletTransferRecord();
        record.setAmount(amount);
        record.setLeverCoin(leverCoin);
        record.setCoin(coin);
        record.setMemberId(member.getId());
        record.setMemberName(member.getUsername());
        //0为转入
        record.setType(0);
        leverWalletTransferRecordRepository.save(record);
        return true;
    }

    private void saveLeverWallet(Member member,Coin coin ,BigDecimal amount,LeverCoin leverCoin){
        LeverWallet newLeverWallet = new LeverWallet();
        newLeverWallet.setCoin(coin);
        newLeverWallet.setMemberId(member.getId());
        newLeverWallet.setMemberName(member.getUsername());
        newLeverWallet.setBalance(amount);
        newLeverWallet.setLeverCoin(leverCoin);
        newLeverWallet.setMobilePhone(member.getMobilePhone());
        newLeverWallet.setEmail(member.getEmail());
        leverWalletRepository.save(newLeverWallet);
    }

    /**
     * 杠杆钱包转钱到普通钱包
     * @param member
     * @param amount
     * @param coin
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean transferTurnOut(Member member, BigDecimal amount,Coin coin,LeverCoin leverCoin){
        LeverWallet leverWallet=leverWalletRepository.findByMemberIdAndLeverCoinAndCoinAndIsLock(member.getId(),leverCoin,coin,BooleanEnum.IS_FALSE);
        if(leverWallet==null||leverWallet.getBalance().compareTo(amount)<0){
            return false;
        }
        MemberWallet memberWallet=memberWalletDao.findByCoinAndMemberId(coin,member.getId());
        memberWalletDao.increaseBalance(memberWallet.getId(), amount);
        leverWallet.setBalance(leverWallet.getBalance().subtract(amount));
        LeverWalletTransferRecord record=new LeverWalletTransferRecord();
        record.setAmount(amount);
        record.setLeverCoin(leverCoin);
        record.setCoin(coin);
        record.setMemberId(member.getId());
        record.setMemberName(member.getUsername());
        //1为转出
        record.setType(1);
        leverWalletTransferRecordRepository.save(record);
        return true;
    }

    public Map<String,Object> listMarginMember(Pageable pageable){
        Page<Object[]> objects=leverWalletRepository.listMarginMember(pageable);
        List<MarginMemberVO> marginMemberVOList=new ArrayList<>();
        if(objects!=null&&objects.getContent()!=null&&objects.getContent().size()>0){
            List<Object[]> objectList=objects.getContent();
            for(Object[] obj:objectList){
                Number memberId= (Number) obj[0];
                LeverCoin leverCoin= (LeverCoin) obj[1];
                MarginMemberVO marginMemberVO=new MarginMemberVO(memberId.longValue(),leverCoin.getId());
                marginMemberVOList.add(marginMemberVO);
            }
        }
        Map map=new HashMap();
        map.put("content",marginMemberVOList);
        map.put("totalPages",objects.getTotalPages());
        map.put("totalElements",objects.getTotalElements());
        return map;
    }

    //解冻
    public MessageResult thawBalance(LeverWallet leverWallet, BigDecimal amount) {
        int ret = leverWalletRepository.thawBalance(leverWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    public LeverWallet findOne(Long id) {
        return leverWalletRepository.findById(id);
    }
}
