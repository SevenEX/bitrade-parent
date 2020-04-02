package cn.ztuo.bitrade.service;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.AppealStatus;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.AppealDao;
import cn.ztuo.bitrade.dao.MemberDao;
import cn.ztuo.bitrade.entity.*;
import cn.ztuo.bitrade.entity.Order;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.vo.AppealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * @author Seven
 * @date 2019年01月23日
 */
@Service
public class AppealService extends BaseService {
    @Autowired
    private AppealDao appealDao;

    @Autowired
    private MemberDao memberDao;

    public Appeal findOne(Long id) {
        Appeal appeal = appealDao.findById(id).orElse(null);
        return appeal;
    }

    public AppealVO findOneAppealVO(long id) {
        return generateAppealVO(findOne(id));
    }

    public Appeal save(Appeal appeal) {
        return appealDao.save(appeal);
    }

    public Appeal findByOrder(Order order) {
        Appeal appeal = appealDao.findByOrderAndStatus(order,AppealStatus.NOT_PROCESSED);
        return appeal;
    }

    /**
     * 条件查询对象 (pageNo pageSize 同时传时分页)
     *
     * @param booleanExpressionList
     * @return
     */
    @Transactional(readOnly = true)
    public PageResult<AppealVO> joinFind(List<BooleanExpression> booleanExpressionList, PageModel pageModel) {
        QAppeal qAppeal = QAppeal.appeal ;
        QBean qBean = Projections.fields(AppealVO.class
                ,qAppeal.id.as("appealId")
                ,qAppeal.order.memberName.as("advertiseCreaterUserName")
                ,qAppeal.order.memberRealName.as("advertiseCreaterName")
                ,qAppeal.order.customerName.as("customerUserName")
                ,qAppeal.order.customerRealName.as("customerName")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.memberName.as("initiatorUsername"):qAppeal.order.customerName.as("initiatorUsername")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.memberRealName.as("initiatorName"):qAppeal.order.customerRealName.as("initiatorName")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.customerName.as("associateUsername"):qAppeal.order.memberName.as("associateUsername")
                ,qAppeal.initiatorId==qAppeal.order.memberId?qAppeal.order.customerRealName.as("associateName"):qAppeal.order.memberRealName.as("associateName")
                ,qAppeal.order.commission.as("fee")
                ,qAppeal.order.number
                ,qAppeal.order.money
                ,qAppeal.order.orderSn.as("orderSn")
                ,qAppeal.order.createTime.as("transactionTime")
                ,qAppeal.createTime.as("createTime")
                ,qAppeal.dealWithTime.as("dealTime")
                ,qAppeal.order.payMode.as("payMode")
                ,qAppeal.order.coin.name.as("coinName")
                ,qAppeal.order.status.as("orderStatus")
                ,qAppeal.isSuccess.as("isSuccess")
                ,qAppeal.order.advertiseType.as("advertiseType")
                ,qAppeal.status
                ,qAppeal.remark
        );
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        JPAQuery<AppealVO> jpaQuery = queryFactory.select(qBean);
        jpaQuery.from(qAppeal);
        if (booleanExpressionList != null)
            jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));

        jpaQuery.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]));

        List<AppealVO> list = jpaQuery.offset((pageModel.getPageNo() - 1) * pageModel.getPageSize())
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]))
                .limit(pageModel.getPageSize()).fetch();
        return new PageResult<>(list, jpaQuery.fetchCount());
    }

    /**
     * 申诉详情
     * @param appeal
     * @return
     */
    private AppealVO generateAppealVO(Appeal appeal){
        Member initialMember = memberDao.findById(appeal.getInitiatorId()).orElse(null);
        Member associateMember = memberDao.findById(appeal.getAssociateId()).orElse(null);
        AppealVO vo = new AppealVO();
        vo.setAppealId(BigInteger.valueOf(appeal.getId()));
        vo.setAssociateName(associateMember.getRealName());
        vo.setAssociateUsername(associateMember.getUsername());
        vo.setInitiatorName(initialMember.getRealName());
        vo.setInitiatorUsername(initialMember.getUsername());
        Order order = appeal.getOrder() ;
        vo.setCoinName(order.getCoin().getName());
        vo.setFee(order.getCommission());
        vo.setMoney(order.getMoney());
        vo.setOrderSn(order.getOrderSn());
        vo.setNumber(order.getNumber());
        vo.setOrderStatus(order.getStatus().getOrdinal());
        vo.setPayMode(order.getPayMode());
        vo.setTransactionTime(order.getCreateTime());
        vo.setIsSuccess(appeal.getIsSuccess().getOrdinal());
        vo.setAdvertiseType(order.getAdvertiseType().getOrdinal());
        vo.setAdvertiseCreaterName(order.getMemberRealName());
        vo.setAdvertiseCreaterUserName(order.getMemberName());
        vo.setCustomerUserName(order.getCustomerName());
        vo.setCustomerName(order.getCustomerRealName());
        vo.setStatus(appeal.getStatus().getOrdinal());
        vo.setCreateTime(appeal.getCreateTime());
        vo.setDealTime(appeal.getDealWithTime());
        vo.setRemark(appeal.getRemark());
        return vo ;
    }

    public Page<Appeal> findAll(Predicate predicate, Pageable pageable) {
        return appealDao.findAll(predicate, pageable);
    }

    public long countAuditing(){
        return appealDao.countAllByStatus(AppealStatus.NOT_PROCESSED);
    }
}
