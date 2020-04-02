package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.dao.LossThresholdRepository;
import cn.ztuo.bitrade.entity.LeverCoin;
import cn.ztuo.bitrade.entity.LossThreshold;
import cn.ztuo.bitrade.enums.PerformActionsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LossThresholdService {
    @Autowired
    private LossThresholdRepository lossThresholdRepository;

    public List<LossThreshold> getByLeverCoin(LeverCoin leverCoin){
        return lossThresholdRepository.findByLeverCoin(leverCoin);
    }

    public void save(LossThreshold lossThreshold){
        lossThresholdRepository.save(lossThreshold);
    }

    public List<LossThreshold> getAll(){
        return lossThresholdRepository.findAll();
    }

    public LossThreshold findByLeverCoinAndThreshold(LeverCoin leverCoin, BigDecimal threshold){
        return lossThresholdRepository.findByLeverCoinAndThreshold(leverCoin,threshold);
    }

    public LossThreshold findById(Long id){
        return lossThresholdRepository.findById(id);
    }

    public void deleteById(Long id){
        lossThresholdRepository.deleteById(id);
    }

    public List<LossThreshold> findAllByStatus(CommonStatus status){
        return lossThresholdRepository.findAllByStatusOrderByThresholdDesc(status);
    }

    public LossThreshold findByLeverCoinAndPerformActions(LeverCoin leverCoin, PerformActionsEnum performActions) {
        return lossThresholdRepository.findByLeverCoinAndPerformActions(leverCoin,performActions);
    }
}
