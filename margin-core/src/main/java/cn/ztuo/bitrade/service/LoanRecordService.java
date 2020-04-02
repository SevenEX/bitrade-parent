package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.LoanRecordRepository;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.LoanRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/25
 */
@Service
public class LoanRecordService {
    @Autowired
    private LoanRecordRepository loanRecordRepository;

    public List<LoanRecord> findByMemberIdAndLeverCoinAndRepayment(Long memberId, LeverCoin leverCoin, BooleanEnum repayment){
        return loanRecordRepository.findByMemberIdAndLeverCoinAndRepayment(memberId,leverCoin,repayment);
    }

    public List<LoanRecord> findByMemberIdAndLeverCoinAndCoinAndRepayment(Long memberId,LeverCoin leverCoin, Coin coin, BooleanEnum repayment){
        return loanRecordRepository.findByMemberIdAndLeverCoinAndCoinAndRepayment(memberId,leverCoin,coin,repayment);
    }

    public List<LoanRecord> findAll(){
        return loanRecordRepository.findAll();
    }

    public Page<LoanRecord> findAll(Predicate predicate, PageModel pageModel){
        return loanRecordRepository.findAll(predicate,pageModel.getPageable());
    }

    public List<LoanRecord> findAllByRepayment(BooleanEnum repayment){
        return loanRecordRepository.findAllByRepayment(repayment);
    }

    public LoanRecord save(LoanRecord loanRecord){
        return loanRecordRepository.save(loanRecord);
    }

    public LoanRecord findById(Long id){
        return loanRecordRepository.findById(id);
    }

    public List<LoanRecord> findByMemberId(Long memberId,LeverCoin leverCoin){
        return loanRecordRepository.findByMemberIdAndLeverCoinOrderByRepayment(memberId,leverCoin);
    }

    public List<LoanRecord> findByMemberIdAndLeverCoin(Long memberId,LeverCoin leverCoin){
        return loanRecordRepository.findByMemberIdAndLeverCoin(memberId,leverCoin);
    }
}
