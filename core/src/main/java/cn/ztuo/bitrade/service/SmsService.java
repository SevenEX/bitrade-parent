package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.SmsDao;
import cn.ztuo.bitrade.dto.SmsDTO;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @author: Seven
 * @date: create in 9:44 2018/6/28
 * @Modified:
 */
@Service
public class SmsService extends BaseService{
    
    @Autowired
    private SmsDao smsDao;

    /**
     * 获取有效的短信配置
     * @return
     */
    public SmsDTO getByStatus(){
        return smsDao.findBySmsStatus();
    }
    
    
}
