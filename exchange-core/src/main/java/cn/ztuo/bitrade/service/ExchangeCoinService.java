package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.util.RedisUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.dao.ExchangeCoinRepository;
import cn.ztuo.bitrade.entity.ExchangeCoin;
import cn.ztuo.bitrade.pagination.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Path;
import java.util.List;

@Service
public class ExchangeCoinService {
    @Autowired
    private ExchangeCoinRepository coinRepository;
    @Autowired
    private RedisUtil redisUtil;

    public List<ExchangeCoin> findAllEnabled() {
        Specification<ExchangeCoin> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> enable = root.get("enable");
            criteriaQuery.where(criteriaBuilder.equal(enable, 1));
            return null;
        };
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "sort");
        Sort sort = Sort.by(order);
        return coinRepository.findAll(spec, sort);
    }

    public ExchangeCoin findByDefault(String defaultSymbol){
        return coinRepository.findExchangeCoinByDefaultSymbol(defaultSymbol);
    }


    public List<ExchangeCoin> findAllByFlag(int flag) {
        Specification<ExchangeCoin> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> enable = root.get("enable");
            Path<Integer> flagPath = root.get("flag");
            criteriaQuery.where(criteriaBuilder.equal(enable, 1),
                    criteriaBuilder.equal(flagPath, flag));
            return null;
        };
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "sort");
        Sort sort = Sort.by(order);
        return coinRepository.findAll(spec, sort);
    }

    public ExchangeCoin findOne(String id) {
        return coinRepository.findById(id).orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletes(String[] ids) {
        for (String id : ids) {
            coinRepository.deleteById(id);
        }
    }

    public ExchangeCoin save(ExchangeCoin exchangeCoin) {
        return coinRepository.save(exchangeCoin);
    }

    public Page<ExchangeCoin> pageQuery(int pageNo, Integer pageSize) {
        Sort orders = Criteria.sortStatic("sort");
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, orders);
        return coinRepository.findAll(pageRequest);
    }

    public ExchangeCoin findBySymbol(String symbol) {
        return coinRepository.findBySymbol(symbol);
    }

    public List<ExchangeCoin> findAll() {
        return coinRepository.findAll();
    }

    public boolean isSupported(String symbol) {
        return findBySymbol(symbol) != null;
    }

    public Page<ExchangeCoin> findAll(Predicate predicate, Pageable pageable) {
        return coinRepository.findAll(predicate, pageable);
    }

    public List<String> getBaseSymbol() {
        return coinRepository.findBaseSymbol();
    }

    public List<String> getBaseSymbolByAreaId(Integer areaId) {
        return coinRepository.findBaseSymbolByAreaId(areaId);
    }

    public List<String> getCoinSymbol(String baseSymbol) {
        return coinRepository.findCoinSymbol(baseSymbol);
    }

    public List<String> getExchangeSymbol(String coinSymbol) {
        return coinRepository.findExchangeSymbol(coinSymbol);
    }

    public List<String> getAllCoin(){
        return coinRepository.findAllCoinSymbol();
    }

    public List<String> getAllSymbol(){
        return coinRepository.findAllSymbol();
    }

    /**
     * 设置交易对儿为非默认
     */
    public void updateDefault() {
        redisUtil.delete(SysConstant.DEFAULT_SYMBOL);
        coinRepository.updateDefaultSymbol();
    }

    public List<ExchangeCoin> findByCoin(String coin) {
        Specification<ExchangeCoin> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> enable = root.get("enable");
            if(StringUtils.isNotEmpty(coin)){
                Path<String> symbol = root.get("symbol");
                criteriaQuery.where(criteriaBuilder.equal(enable, 1),criteriaBuilder.like(symbol, "%"+coin+"%"));
            }else {
                criteriaQuery.where(criteriaBuilder.equal(enable, 1));
            }
            return null;
        };
        Sort.Order order1 = new Sort.Order(Sort.Direction.ASC, "sort");
        Sort.Order order2 = new Sort.Order(Sort.Direction.ASC, "symbol");
        Sort sort = Sort.by(order1,order2);
        return coinRepository.findAll(spec, sort);
    }

    public List<ExchangeCoin> findByCoin(String coin,Integer areaId) {
        Specification<ExchangeCoin> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Path<String> enable = root.get("enable");
            Path<Integer> area = root.get("areaId");
            if(StringUtils.isNotEmpty(coin)){
                Path<String> symbol = root.get("symbol");
                criteriaQuery.where(criteriaBuilder.equal(enable, 1),criteriaBuilder.like(symbol, "%"+coin+"%"),criteriaBuilder.equal(area,areaId));
            }else {
                criteriaQuery.where(criteriaBuilder.equal(enable, 1),criteriaBuilder.equal(area,areaId));
            }
            return null;
        };
        Sort.Order order1 = new Sort.Order(Sort.Direction.ASC, "sort");
        Sort.Order order2 = new Sort.Order(Sort.Direction.ASC, "symbol");
        Sort sort = Sort.by(order1,order2);
        return coinRepository.findAll(spec, sort);
    }
}
