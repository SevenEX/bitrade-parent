package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.RiskRecordRepository;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.RiskRecord;
import cn.ztuo.bitrade.enums.PerformActionsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskRecordService {
    @Autowired
    private RiskRecordRepository riskRecordRepository;

    public RiskRecord save(RiskRecord riskRecord){
        return riskRecordRepository.save(riskRecord);
    }

    public List<RiskRecord> findByMemberId(Long memberId){
        return riskRecordRepository.findByMemberId(memberId);
    }

    public List<RiskRecord> findByMemberIdAndLeverCoin(Long memberId, LeverCoin leverCoin){
        return riskRecordRepository.findByMemberIdAndLeverCoin(memberId,leverCoin);
    }

    public List<RiskRecord> findByPerformActionsAndMemberId(PerformActionsEnum performActions, Long memberId){
        return riskRecordRepository.findByPerformActionsAndMemberId(performActions,memberId);
    }
}
