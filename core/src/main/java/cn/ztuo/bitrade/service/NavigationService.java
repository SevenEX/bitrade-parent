package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.NavigationDao;
import cn.ztuo.bitrade.entity.Navigation;
import cn.ztuo.bitrade.entity.QNavigation;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author MrGao
 * @description
 * @date 2018/1/9 10:00
 */
@Service
public class NavigationService extends BaseService {
    @Autowired
    private NavigationDao navigationDao;

    public Navigation save(Navigation navigation) {
        return navigationDao.save(navigation);
    }

    public Navigation findOne(Long id) {
        return navigationDao.findById(id).orElse(null);
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            navigationDao.deleteById(id);
        }
    }


    /**
     * 条件查询对象 pageNo pageSize 同时传时分页
     *
     * @param booleanExpressionList
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<Navigation> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        JPAQuery<Navigation> jpaQuery = queryFactory.selectFrom(QNavigation.navigation);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        }
        jpaQuery.orderBy(QNavigation.navigation.createTime.desc());
        List<Navigation> list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        long count = jpaQuery.fetchCount();
        PageResult<Navigation> page = new PageResult<>(list, pageNo, pageSize, count);
        return page;
    }

    public List<Navigation> findAllByStatusNotAndTypeAndLocale(String type,String locale) {
        return navigationDao.findAllByStatusNotAndTypeAndLocale(type,locale);
    }
}
