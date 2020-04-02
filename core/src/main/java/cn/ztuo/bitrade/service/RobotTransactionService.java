package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.RobotTransactionDao;
import cn.ztuo.bitrade.entity.RobotTransaction;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: RobotTransactionService
 * @author: MrGao
 * @create: 2019/04/30 14:16
 */
@Service
public class RobotTransactionService extends BaseService<RobotTransaction> {
    @Autowired
    private RobotTransactionDao dao ;


    public RobotTransaction save(RobotTransaction transaction){
        return dao.save(transaction);
    }

}
