package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.SignStatus;
import cn.ztuo.bitrade.dao.SignDao;
import cn.ztuo.bitrade.entity.Sign;
import cn.ztuo.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author MrGao
 * @Description:
 * @date 2018/5/311:11
 */
@Service
public class SignService extends TopBaseService<Sign, SignDao> {


    @Autowired
    public void setDao(SignDao dao) {
        super.setDao(dao);
    }

    public Sign fetchUnderway() {
        return dao.findByStatus(SignStatus.UNDERWAY);
    }

    /**
     * 提前关闭
     *
     * @param sign 提前关闭
     */
    public void earlyClosing(Sign sign) {
        sign.setStatus(SignStatus.FINISH);
        dao.save(sign);
    }

}
