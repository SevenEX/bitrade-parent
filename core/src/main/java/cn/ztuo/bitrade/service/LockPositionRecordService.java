package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.TransactionType;
import cn.ztuo.bitrade.dao.LockPositionRecordDao;
import cn.ztuo.bitrade.dao.MemberTransactionDao;
import cn.ztuo.bitrade.dao.MemberWalletDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class LockPositionRecordService extends BaseService {
    @Autowired
    private LockPositionRecordDao lockPositionRecordDao;
    @Autowired
    private MemberWalletDao memberWalletDao;
    @Autowired
    private MemberTransactionDao memberTransactionDao;

    public LockPositionRecord findById(Long id){
        return lockPositionRecordDao.findById(id).orElse(null);
    }

    public Page<LockPositionRecord> findAll(Predicate predicate, PageModel pageModel){
        return lockPositionRecordDao.findAll(predicate,pageModel.getPageable());
    }

    public LockPositionRecord save(LockPositionRecord lockPositionRecord){
        return lockPositionRecordDao.save(lockPositionRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unlockByTime(Date now){
        List<LockPositionRecord> lockPositionRecordList=lockPositionRecordDao.findByStatusAndUnlockTime(CommonStatus.NORMAL,now);
        if(lockPositionRecordList!=null&&lockPositionRecordList.size()>0){
            List<Long> ids=new ArrayList<>();
            List<MemberTransaction> memberTransactionList=new ArrayList<>();
            for(LockPositionRecord lockPositionRecord:lockPositionRecordList){
                memberWalletDao.thawBalance(lockPositionRecord.getWalletId(),lockPositionRecord.getAmount());
                MemberTransaction memberTransaction=new MemberTransaction();
                memberTransaction.setMemberId(lockPositionRecord.getMemberId());
                memberTransaction.setAmount(lockPositionRecord.getAmount());
                memberTransaction.setSymbol(lockPositionRecord.getCoin().getUnit());
                memberTransaction.setFee(BigDecimal.ZERO);
                memberTransaction.setType(TransactionType.UNLOCK_POSITION);
                memberTransaction.setAddress("");
                memberTransactionList.add(memberTransaction);
                ids.add(lockPositionRecord.getId());
            }
            lockPositionRecordDao.unlockByIds(ids,CommonStatus.ILLEGAL);
            memberTransactionDao.saveAll(memberTransactionList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public MessageResult lockPosition(MemberWallet memberWallet, BigDecimal amount, Member member,String reason,Date unlockTime){
        memberWalletDao.freezeBalance(memberWallet.getId(),amount);
        LockPositionRecord lockPositionRecord=new LockPositionRecord();
        lockPositionRecord.setMemberId(memberWallet.getMemberId());
        lockPositionRecord.setCoin(memberWallet.getCoin());
        lockPositionRecord.setCreateTime(new Date());
        lockPositionRecord.setMemberName(member.getUsername());
        lockPositionRecord.setStatus(CommonStatus.NORMAL);
        lockPositionRecord.setReason(reason);
        lockPositionRecord.setUnlockTime(unlockTime);
        lockPositionRecord.setAmount(amount);
        lockPositionRecord.setWalletId(memberWallet.getId());
        lockPositionRecordDao.save(lockPositionRecord);
        MemberTransaction memberTransaction=new MemberTransaction();
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setAmount(amount.negate());
        memberTransaction.setSymbol(memberWallet.getCoin().getUnit());
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setType(TransactionType.LOCK_POSITION);
        memberTransaction.setAddress("");
        memberTransactionDao.save(memberTransaction);
        return MessageResult.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public MessageResult unlock(LockPositionRecord lockPositionRecord){
        lockPositionRecordDao.unlockById(lockPositionRecord.getId(),CommonStatus.ILLEGAL);
        memberWalletDao.thawBalance(lockPositionRecord.getWalletId(),lockPositionRecord.getAmount());
        MemberTransaction memberTransaction=new MemberTransaction();
        memberTransaction.setMemberId(lockPositionRecord.getMemberId());
        memberTransaction.setAmount(lockPositionRecord.getAmount());
        memberTransaction.setSymbol(lockPositionRecord.getCoin().getUnit());
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setType(TransactionType.UNLOCK_POSITION);
        memberTransaction.setAddress("");
        memberTransactionDao.save(memberTransaction);
        return MessageResult.success();
    }

    public List<LockPositionRecord> findByMemberIdAndCoinAndStatus(Long memberId, Coin coin, CommonStatus status){
        return lockPositionRecordDao.findByMemberIdAndCoinAndStatus(memberId,coin,status);
    }
}
