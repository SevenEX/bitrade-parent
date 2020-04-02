package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.PaymentHistoryRepository;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.PaymentHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentHistoryService {
    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    public List<PaymentHistory> findByMemberIdAndLeverCoinAndCoin(Long memberId, LeverCoin leverCoin,Coin coin){
        return paymentHistoryRepository.findByMemberIdAndLeverCoinAndCoin(memberId,leverCoin,coin);
    }

    public PaymentHistory findById(Long id){
        return paymentHistoryRepository.findById(id);
    }

    public void save(PaymentHistory paymentHistory){
        paymentHistoryRepository.save(paymentHistory);
    }

    public Page<PaymentHistory> findAll(Predicate predicate, PageModel pageModel){
        return paymentHistoryRepository.findAll(predicate,pageModel.getPageable());
    }
}
