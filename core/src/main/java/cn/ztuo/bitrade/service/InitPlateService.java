package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.dao.InitPlateDao;
import cn.ztuo.bitrade.entity.InitPlate;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InitPlateService extends BaseService {

    @Autowired
    private RedisUtil redisUtil ;

    @Autowired
    private InitPlateDao initPlateDao ;
    public InitPlate findInitPlateBySymbol(String symbol) {
        return initPlateDao.findInitPlateBySymbol(symbol);
    }

    public InitPlate save(InitPlate initPlate){
        return initPlateDao.save(initPlate);
    }

    public InitPlate saveAndFlush(InitPlate initPlate) {
       return initPlateDao.saveAndFlush(initPlate);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(long id){
        initPlateDao.deleteById(id);
    }

    public Page<InitPlate> findAllByPage(Criteria<InitPlate> specification, PageRequest pageRequest) {
        return initPlateDao.findAll(specification,pageRequest);
    }
    public InitPlate findByInitPlateId(long id){
        return initPlateDao.findById(id).orElse(null);
    }

    @Override
    public List<InitPlate> findAll(){
        return initPlateDao.findAll();
    }

    public List<String> findAllSymbols() {
        Object object = redisUtil.get(SysConstant.EXCHANGE_INIT_PLATE_ALL_SYMBOLS);
        if(object!=null){
            return (List<String>) object;
        }else {
            List<InitPlate> initPlates = initPlateDao.findAll();
            List<String> symbols = new ArrayList<>();
            for (InitPlate initPlate :initPlates){
                symbols.add(initPlate.getSymbol());
            }
            redisUtil.set(SysConstant.EXCHANGE_INIT_PLATE_ALL_SYMBOLS,symbols);
            return symbols;
        }
    }
}
