package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.BusinessAuthDepositDao;
import cn.ztuo.bitrade.entity.BusinessAuthDeposit;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhang yingxin
 * @date 2018/5/5
 */
@Service
public class BusinessAuthDepositService extends BaseService {
    @Autowired
    private BusinessAuthDepositDao businessAuthDepositDao;

    public Page<BusinessAuthDeposit> findAll(Predicate predicate, PageModel pageModel) {
        return businessAuthDepositDao.findAll(predicate, pageModel.getPageable());
    }

    public List<BusinessAuthDeposit> findAllByStatus(CommonStatus status){
        return businessAuthDepositDao.findAllByStatus(status);
    }

    public BusinessAuthDeposit findById(Long id){
        return businessAuthDepositDao.findById(id).orElse(null);
    }

    public void save(BusinessAuthDeposit businessAuthDeposit){
        businessAuthDepositDao.save(businessAuthDeposit);
    }

    public void update(BusinessAuthDeposit businessAuthDeposit){
        businessAuthDepositDao.save(businessAuthDeposit);
    }
}
