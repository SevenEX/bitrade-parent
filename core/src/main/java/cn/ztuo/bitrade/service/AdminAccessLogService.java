package cn.ztuo.bitrade.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.AdminAccessLogDao;
import cn.ztuo.bitrade.entity.AdminAccessLog;
import cn.ztuo.bitrade.entity.QAdmin;
import cn.ztuo.bitrade.entity.QAdminAccessLog;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Seven
 * @date 2019年12月19日
 */
@Service
public class AdminAccessLogService extends BaseService {

    @Autowired
    private AdminAccessLogDao adminAccessLogDao;

    public AdminAccessLog saveLog(AdminAccessLog adminAccessLog) {
        return adminAccessLogDao.save(adminAccessLog);
    }

    public List<AdminAccessLog> queryAll() {
        return adminAccessLogDao.findAll();
    }

    public AdminAccessLog queryById(Long id) {
        return adminAccessLogDao.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public PageResult<AdminAccessLog> query(List<Predicate> predicateList, Integer pageNo, Integer pageSize) {
        List<AdminAccessLog> list;
        JPAQuery<AdminAccessLog> jpaQuery = queryFactory.selectFrom(QAdminAccessLog.adminAccessLog);
        if (predicateList != null && predicateList.size() > 0)
            jpaQuery.where(predicateList.toArray(new Predicate[predicateList.size()]));
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());//添加总条数
    }

    public Page<AdminAccessLog> page(List<BooleanExpression> predicates, PageModel pageModel){
        JPAQuery<AdminAccessLog>  query = queryFactory.select(
                Projections.fields(AdminAccessLog.class,
                        QAdminAccessLog.adminAccessLog.id.as("id"),
                        QAdminAccessLog.adminAccessLog.accessIp.as("accessIp"),
                        QAdminAccessLog.adminAccessLog.accessMethod.as("accessMethod"),
                        QAdminAccessLog.adminAccessLog.accessTime.as("accessTime"),
                        QAdminAccessLog.adminAccessLog.adminId.as("adminId"),
                        QAdminAccessLog.adminAccessLog.uri.as("uri"),
                        QAdminAccessLog.adminAccessLog.module.as("module"),
                        QAdminAccessLog.adminAccessLog.operation.as("operation"),
                        QAdmin.admin.username.as("adminName"))
        ).from(QAdminAccessLog.adminAccessLog,QAdmin.admin).where(predicates.toArray(new BooleanExpression[predicates.size()])) ;
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers() ;
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));
        long total = query.fetchCount() ;
        query.offset(pageModel.getPageSize()*(pageModel.getPageNo()-1)).limit(pageModel.getPageSize());
        List<AdminAccessLog> list = query.fetch() ;
        return new PageImpl<AdminAccessLog>(list,pageModel.getPageable(),total);
    }
}
