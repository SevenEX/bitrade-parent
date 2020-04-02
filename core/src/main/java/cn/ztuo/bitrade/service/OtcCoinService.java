package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.core.Model;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.dao.OtcCoinDao;
import cn.ztuo.bitrade.entity.OtcCoin;
import cn.ztuo.bitrade.entity.QOtcCoin;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author MrGao
 * @description
 * @date 2018/1/11 13:45
 */
@Service
public class OtcCoinService extends BaseService {
    @Autowired
    private OtcCoinDao otcCoinDao;

    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<OtcCoin> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        List<OtcCoin> list;
        JPAQuery<OtcCoin> jpaQuery = queryFactory.selectFrom(QOtcCoin.otcCoin);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        }
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }


    public OtcCoin save(OtcCoin otcCoin) {
        return otcCoinDao.save(otcCoin);
    }
    @Override
    public List<OtcCoin> findAll() {
        return otcCoinDao.findAll();
    }

    public OtcCoin findOne(Long id) {
        return otcCoinDao.findById(id).orElse(null);
    }

    public OtcCoin findByUnit(String unit) {
        return otcCoinDao.findOtcCoinByUnit(unit);
    }

    public List<Map<String, String>> getAllNormalCoin() throws Exception {
        return new Model("otc_coin")
                .field("id,name,name_cn as nameCn,unit,sell_min_amount,sort,buy_min_amount")
                .where("status=?", CommonStatus.NORMAL.ordinal())
                .order("sort asc").select();
    }

    public List<OtcCoin> getNormalCoin() {
        return otcCoinDao.findAllByStatus(CommonStatus.NORMAL);
    }

    /**
     * @author MrGao
     * @description 分页请求
     * @date 2018/1/11 15:04
     */
    public Page<OtcCoin> pageQuery(Integer pageNo, Integer pageSize, String name, String nameCn) {
        //排序方式
        Sort orders = Criteria.sortStatic("sort");
        //分页参数
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, orders);
        //查询条件
        Criteria<OtcCoin> specification = new Criteria<OtcCoin>();
        specification.add(Restrictions.like("name", name, false));
        specification.add(Restrictions.like("nameCn", nameCn, false));
        return otcCoinDao.findAll(specification, pageRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletes(Long[] ids) {
        for (long id : ids) {
            otcCoinDao.deleteById(id);
        }
    }

    public Page<OtcCoin> findAll(Predicate predicate, Pageable pageable) {
        return otcCoinDao.findAll(predicate, pageable);
    }

    public List<String> findAllUnits(){
        List<String> list = otcCoinDao.findAllUnits();
        return  list ;
    }

    public OtcCoin findOtcCoinByUnitAndStatus(String coinUnit,CommonStatus commonStatus) {
        return otcCoinDao.findOtcCoinByUnitAndStatus(coinUnit,commonStatus);
    }

    public List<String> findAllUnitsByStatus(){
        List<String> list = otcCoinDao.findAllUnitsByStatus();
        return  list ;
    }

}
