package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.SysHelpClassification;
import cn.ztuo.bitrade.dao.SysHelpDao;
import cn.ztuo.bitrade.entity.QSysHelp;
import cn.ztuo.bitrade.entity.SysHelp;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


/**
 * @author MrGao
 * @description
 * @date 2018/1/9 10:00
 */
@Service
public class SysHelpService extends BaseService {
    @Autowired
    private SysHelpDao sysHelpDao;

    public SysHelp save(SysHelp sysHelp) {
        return sysHelpDao.save(sysHelp);
    }

    public List<SysHelp> findAll(Sort sort) {
        return sysHelpDao.findAll(sort);
    }

    public SysHelp findOne(Long id) {
        return sysHelpDao.findById(id).orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            sysHelpDao.deleteById(id);
        }
    }

    public int getMaxSort(){
        return sysHelpDao.findMaxSort();
    }

    public List<SysHelp> findBySysHelpClassification(SysHelpClassification sysHelpClassification, String locale) {
        return sysHelpDao.findAllBySysHelpClassificationAndStatusNotAndLocale(sysHelpClassification,CommonStatus.ILLEGAL, locale);
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
    public PageResult<SysHelp> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        JPAQuery<SysHelp> jpaQuery = queryFactory.selectFrom(QSysHelp.sysHelp);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        }
        jpaQuery.orderBy(QSysHelp.sysHelp.createTime.desc());
        List<SysHelp> list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        long count = jpaQuery.fetchCount();
        PageResult<SysHelp> page = new PageResult<>(list, pageNo, pageSize, count);
        return page;
    }

    public Page<SysHelp> findAll(Predicate predicate, Pageable pageable) {
        return sysHelpDao.findAll(predicate, pageable);
    }

    /**
     * 根据分类分页查询
     * @param pageNo
     * @param pageSize
     * @param cate
     * @return
     */
    public Page<SysHelp> findByCondition(int pageNo,int pageSize,SysHelpClassification cate, String locale){
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "sort"));
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Specification specification = new Specification() {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

            @Override
            public javax.persistence.criteria.Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                predicates.add(criteriaBuilder.equal(root.get("sysHelpClassification"),cate));
                predicates.add(criteriaBuilder.equal(root.get("locale"),locale));
                return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[predicates.size()]));
            }
        };
        return sysHelpDao.findAll(specification,pageable);
    }

    public List<SysHelp> getgetCateTops(String cate,String locale){
        return sysHelpDao.getCateTop(cate,locale);
    }

    public List<SysHelp> findAllByStatusNotAndSortAndLocale(String locale) {
        return sysHelpDao.findAllByStatusNotAndSortAndLocale(locale);
    }
}
