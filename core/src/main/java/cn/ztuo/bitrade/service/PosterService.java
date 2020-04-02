package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.PosterDao;
import cn.ztuo.bitrade.entity.Poster;
import cn.ztuo.bitrade.entity.QPoster;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Paradise
 */
@Service
public class PosterService extends BaseService<Poster> {

    @Resource
    private PosterDao posterDao;

    public Poster save(Poster poster) {
        return posterDao.save(poster);
    }

    public List<Poster> findAll(Sort sort) {
        return posterDao.findAll(sort);
    }

    @Override
    public List<Poster> findAll() {
        return posterDao.findAll();
    }

    public Poster findOne(Long id) {
        return posterDao.findById(id).orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            posterDao.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public PageResult<Poster> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        JPAQuery<Poster> jpaQuery = queryFactory.selectFrom(QPoster.poster);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[0]));
        }
        jpaQuery.orderBy(QPoster.poster.createTime.desc());
        List<Poster> list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        long count = jpaQuery.fetchCount();
        return new PageResult<>(list, pageNo, pageSize, count);
    }

    public Page<Poster> findAll(Predicate predicate, Pageable pageable) {
        return posterDao.findAll(predicate, pageable);
    }

    public List<Poster> findAllByLocale(String locale) {
        return posterDao.findAllByLocale(locale);
    }
}
