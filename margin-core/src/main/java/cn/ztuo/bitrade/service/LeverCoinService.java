package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.dao.LeverCoinRepository;
import cn.ztuo.bitrade.entity.LeverCoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeverCoinService {
    @Autowired
    private LeverCoinRepository leverCoinRepository;

    public LeverCoin getBySymbol(String symbol){
        return leverCoinRepository.getBySymbol(symbol);
    }

//    public void save(LeverCoin leverCoin){
//        leverCoinRepository.save(leverCoin);
//    }

    public LeverCoin save(LeverCoin leverCoin){
        return  leverCoinRepository.save(leverCoin);
    }

    public List<LeverCoin> findAll(){
        return leverCoinRepository.findAll();
    }

    public List<LeverCoin> findByEnable(BooleanEnum enable){
        return leverCoinRepository.findByEnable(enable);
    }

    /**
     * 查询所有
     * @param predicate
     * @param pageable
     * @return
     */
    public Page<LeverCoin> findAll(Predicate predicate, Pageable pageable) {
        return leverCoinRepository.findAll(predicate, pageable);
    }

    /**
     * 根据ID查找
     * @param id
     * @return
     */
    public LeverCoin findOne(Long id) {
        return leverCoinRepository.findById(id);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletes(Long[] ids) {
        for (Long id : ids) {
            leverCoinRepository.deleteById(id);
        }
    }

    /**
     * 获取所有可用基币
     * @return
     */
    public List<String> getBaseSymbol() {
        return leverCoinRepository.findBaseSymbol();
    }

    /**
     * 查询获得的币对
     * @param baseSymbol
     * @return
     */
    public List<String> getCoinSymbol(String baseSymbol) {
        return leverCoinRepository.findCoinSymbol(baseSymbol);
    }

    public List<LeverCoin> findLeverCoinByCoinUnitAndEnable(String coinUnit, BooleanEnum enable) {
        return leverCoinRepository.findLeverCoinsBySymbolLikeAndEnable("%"+coinUnit+"%",enable);
    }
}
