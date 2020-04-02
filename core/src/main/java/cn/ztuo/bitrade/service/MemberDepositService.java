package cn.ztuo.bitrade.service;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.dao.MemberDepositDao;
import cn.ztuo.bitrade.entity.MemberDeposit;
import cn.ztuo.bitrade.entity.QMember;
import cn.ztuo.bitrade.entity.QMemberDeposit;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.vo.MemberDepositVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberDepositService extends BaseService<MemberDeposit> {

    @Autowired
    private MemberDepositDao memberDepositDao ;

    public Page<MemberDepositVO> page(List<BooleanExpression> predicates,PageModel pageModel){
        JPAQuery<MemberDepositVO> query = queryFactory.select(Projections.fields(MemberDepositVO.class,
                QMemberDeposit.memberDeposit.id.as("id"),
                QMember.member.username,
                QMember.member.id.as("memberId"),
                QMemberDeposit.memberDeposit.address,
                QMemberDeposit.memberDeposit.amount,
                QMemberDeposit.memberDeposit.txid,
                QMemberDeposit.memberDeposit.createTime.as("createTime"),
                QMemberDeposit.memberDeposit.unit)).from(QMember.member,QMemberDeposit.memberDeposit)
                .where(predicates.toArray(new BooleanExpression[predicates.size()]));
        List<OrderSpecifier> orderSpecifiers = pageModel.getOrderSpecifiers();
        query.orderBy(orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()])) ;
        long total = query.fetchCount() ;
        query.offset(pageModel.getPageSize()*(pageModel.getPageNo()-1)).limit(pageModel.getPageSize());
        List<MemberDepositVO> list = query.fetch() ;
        return new PageImpl<MemberDepositVO>(list,pageModel.getPageable(),total);
    }

}
