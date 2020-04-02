package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.LeverWalletTransferRecordRepository;
import cn.ztuo.bitrade.entity.LeverWalletTransferRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeverWalletTransferRecordService {
    @Autowired
    private LeverWalletTransferRecordRepository leverWalletTransferRecordRepository;

    public List<LeverWalletTransferRecord> findByMemberId(Long memberId){
        return leverWalletTransferRecordRepository.findByMemberId(memberId);
    }

    public List<LeverWalletTransferRecord> findByMemberIdAndType(Long memberId,Integer type){
        return leverWalletTransferRecordRepository.findByMemberIdAndType(memberId,type);
    }

    public void create(LeverWalletTransferRecord leverWalletTransferRecord){
        leverWalletTransferRecordRepository.save(leverWalletTransferRecord);
    }

    public Page<LeverWalletTransferRecord> findAll(Predicate predicate, PageModel pageModel){
        return leverWalletTransferRecordRepository.findAll(predicate,pageModel.getPageable());
    }
}
