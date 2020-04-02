package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.SettlementCoinDao;
import cn.ztuo.bitrade.entity.SettlementCoin;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrGao
 * @description
 * @date 2017/12/29 14:50
 */
@Service
public class SettlementCoinService extends BaseService {
    @Autowired
    private SettlementCoinDao settlementCoinDao;

    public Page<SettlementCoin> findAll(PageModel pageModel, String coinName, CommonStatus status) {
        Sort orders = Criteria.sortStatic("createTime.desc");
        //分页参数
        PageRequest pageRequest = PageRequest.of(pageModel.getPageNo()-1, pageModel.getPageSize(), orders);
        if(StringUtils.isNotEmpty(coinName)||status !=null){
            //查询条件
            Criteria<SettlementCoin> specification = new Criteria<SettlementCoin>();
            if(StringUtils.isNotEmpty(coinName))
                specification.add(Restrictions.eq("coinName", coinName, false));
            if(status !=null)
                specification.add(Restrictions.eq("status", status, false));
            return settlementCoinDao.findAll(specification, pageRequest);
        }

        return settlementCoinDao.findAll(pageRequest);
    }

    public SettlementCoin save(SettlementCoin coin) {
        return settlementCoinDao.save(coin);
    }

    public void deleteOne(String coinName) {
        settlementCoinDao.deleteById(coinName);
    }

    public List<String> getBaseSymbol() {
        return settlementCoinDao.findBaseSymbol();
    }
}
