package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.HotTransferRecordDao;
import cn.ztuo.bitrade.entity.HotTransferRecord;
import cn.ztuo.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotTransferRecordService extends TopBaseService<HotTransferRecord,HotTransferRecordDao> {

    @Autowired
    public void setDao(HotTransferRecordDao dao) {
        super.setDao(dao);
    }
}
