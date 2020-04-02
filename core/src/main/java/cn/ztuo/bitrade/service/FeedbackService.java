package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.FeedbackDao;
import cn.ztuo.bitrade.entity.Feedback;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Seven
 * @date 2019年03月19日
 */
@Service
public class FeedbackService extends BaseService {
    @Autowired
    private FeedbackDao feedbackDao;

    public Feedback save(Feedback feedback){
        return feedbackDao.save(feedback);
    }
}
