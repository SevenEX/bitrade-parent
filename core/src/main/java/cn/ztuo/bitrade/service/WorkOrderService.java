package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.WorkOrderType;
import cn.ztuo.bitrade.dao.WorkOrderDao;
import cn.ztuo.bitrade.entity.QWorkOrder;
import cn.ztuo.bitrade.entity.WorkOrder;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class WorkOrderService extends BaseService {
    @Autowired
    private WorkOrderDao workOrderDao;

    public WorkOrder save(WorkOrder workOrder) {
        return workOrderDao.save(workOrder);
    }

    public List<WorkOrder> findAll(Sort sort) {
        return workOrderDao.findAll(sort);
    }

    public WorkOrder findOne(Long id) {
        return workOrderDao.findById(id).orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            workOrderDao.deleteById(id);
        }
    }

    public List<WorkOrder> findByType(WorkOrderType workOrderType) {
        return workOrderDao.findAllByType(workOrderType);
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
    public PageResult<WorkOrder> queryWhereOrPage(List<BooleanExpression> booleanExpressionList, Integer pageNo, Integer pageSize) {
        JPAQuery<WorkOrder> jpaQuery = queryFactory.selectFrom(QWorkOrder.workOrder);
        if (booleanExpressionList != null) {
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        }
        jpaQuery.orderBy(QWorkOrder.workOrder.createTime.desc());
        List<WorkOrder> list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        long count = jpaQuery.fetchCount();
        PageResult<WorkOrder> page = new PageResult<>(list, pageNo, pageSize, count);
        return page;
    }

    public Page<WorkOrder> findAll(Predicate predicate, Pageable pageable) {
        return workOrderDao.findAll(predicate, pageable);
    }

    /**
     * 根据分类分页查询
     *
     * @param pageNo
     * @param pageSize
     * @param workOrder
     * @return
     */
    public Page<WorkOrder> findByCondition(int pageNo, int pageSize, WorkOrder workOrder, String keyWords) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "createTime"));
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        List<BooleanExpression> el = new ArrayList<>();
        if (workOrder.getMemberId() != null) {
            el.add(QWorkOrder.workOrder.memberId.eq(workOrder.getMemberId()));
        }
        if (workOrder.getType() != null) {
            el.add(QWorkOrder.workOrder.type.eq(workOrder.getType()));
        }
        if (workOrder.getStatus() != null) {
            el.add(QWorkOrder.workOrder.status.eq(workOrder.getStatus()));
        }
        if (StringUtils.isNotBlank(keyWords)) {
            el.add(QWorkOrder.workOrder.contact.like("%" + keyWords + "%")
                    .or(QWorkOrder.workOrder.id.like("%" + keyWords + "%")));
        }
        return workOrderDao.findAll(PredicateUtils.getPredicate(el), pageable);
    }
}
