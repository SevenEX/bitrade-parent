package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.CoinAreaDao;
import cn.ztuo.bitrade.entity.CoinArea;
import cn.ztuo.bitrade.service.Base.BaseService;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author MrGao
 * @description
 * @date 2017/12/29 14:50
 */
@Service
public class CoinAreaService extends BaseService {
    @Autowired
    private CoinAreaDao coinAreaDao;

    @Override
    public List<CoinArea> findAll() {
        return coinAreaDao.findAll();
    }

    public CoinArea findOne(Long id) {
        return coinAreaDao.findById(id).orElse(null);
    }

    public Page<CoinArea> findAll(Predicate predicate, Pageable pageable) {
        return coinAreaDao.findAll(predicate, pageable);
    }

    public CoinArea save(CoinArea coin) {
        return coinAreaDao.save(coin);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletes(Long[] ids) {
        for (Long id : ids) {
            coinAreaDao.deleteById(id);
        }
    }
}
