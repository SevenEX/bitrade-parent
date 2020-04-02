package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.RewardStatisticsDao;
import cn.ztuo.bitrade.entity.RewardRecord;
import cn.ztuo.bitrade.entity.RewardStatistics;
import cn.ztuo.bitrade.service.Base.BaseService;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Seven
 * @date 2019年03月08日
 */
@Service
public class RewardStatisticsService extends BaseService {
    @Autowired
    private RewardStatisticsDao rewardStatisticsDao;

    /**
     * @param pageModel 分页对象
     * @return
     */
    @Transactional(readOnly = true)
    public Page<Object[]> findAll(long mamberId,String month, PageModel pageModel) {
        Pageable pageable = PageRequest.of(pageModel.getPageNo()- 1, pageModel.getPageSize());
        return rewardStatisticsDao.findAll(mamberId,month,pageable);
    }

    @Transactional(readOnly = true)
    public List<Object[]> findAll(String month, int limit) {
        return rewardStatisticsDao.findAllList(month,limit);
    }
}
