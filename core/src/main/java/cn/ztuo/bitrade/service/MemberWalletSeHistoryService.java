package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.MemberWalletSeHistoryDao;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberWalletSeHistoryService extends BaseService {
    @Autowired
    private MemberWalletSeHistoryDao memberWalletDao;



}
