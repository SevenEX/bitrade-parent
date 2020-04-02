package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.PoundageConvertEthDao;
import cn.ztuo.bitrade.entity.PoundageConvertEth;
import cn.ztuo.bitrade.service.Base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PoundageConvertEthService extends BaseService<PoundageConvertEth> {

    @Autowired
    private PoundageConvertEthDao poundageConvertEthDao ;

    /**
     * 保存
     * @param poundageConvertEth
     * @return
     */
    PoundageConvertEth save(PoundageConvertEth poundageConvertEth){
        return poundageConvertEthDao.save(poundageConvertEth);
    }

    /**
     * 根据日期查询折合手续费
     * @param startDate
     * @param endDate
     * @return
     */
    public BigDecimal getAmountEthByDate(String startDate, String endDate) {
        return poundageConvertEthDao.getAmountEthByDate(startDate,endDate);
    }

    /**
     * 获取用户手续费 机器人排除之外
     * @param arivedTime
     * @return
     */
    public BigDecimal getAmountEth(String staTime ,String arivedTime) {
        String startTime = staTime+" 00:00:00";
        String endTime = arivedTime+" 23:59:59";
        return poundageConvertEthDao.getAmountEth(startTime,endTime);
    }
}
