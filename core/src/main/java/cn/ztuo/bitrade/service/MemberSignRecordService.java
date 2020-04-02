package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.MemberSignRecordDao;
import cn.ztuo.bitrade.entity.MemberSignRecord;
import cn.ztuo.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author MrGao
 * @Description:
 * @date 2018/5/410:19
 */
@Service
public class MemberSignRecordService extends TopBaseService<MemberSignRecord, MemberSignRecordDao> {
    @Autowired
    public void setDao(MemberSignRecordDao dao) {
        super.setDao(dao);
    }
}
