package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.DepositStatusEnum;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.DepositRecordDao;
import cn.ztuo.bitrade.entity.DepositRecord;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/7
 */
@Service
public class DepositRecordService extends BaseService {
    @Autowired
    private DepositRecordDao depositRecordDao;

    public List<DepositRecord> getAll(){
        return depositRecordDao.findAll();
    }

    public Page<DepositRecord> list(Predicate predicate, PageModel pageModel){
        return depositRecordDao.findAll(predicate,pageModel.getPageable());
    }

    public List<DepositRecord> findAll(Predicate predicate){
        return (List<DepositRecord>) depositRecordDao.findAll(predicate);
    }

    public DepositRecord findOne(String id){
        return depositRecordDao.findById(id);
    }

    public void update(DepositRecord depositRecord){
        depositRecordDao.save(depositRecord);
    }

    public void create(DepositRecord depositRecord){
        depositRecordDao.save(depositRecord);
    }

    public List<DepositRecord> findByMemberAndStatus(Member member, DepositStatusEnum status){
        return depositRecordDao.findByMemberAndStatus(member,status);
    }

    public DepositRecord save(DepositRecord depositRecord){
        return depositRecordDao.save(depositRecord);
    }
}
