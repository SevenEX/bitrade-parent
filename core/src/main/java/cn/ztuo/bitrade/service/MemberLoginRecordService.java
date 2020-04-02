package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.LoginStatus;
import cn.ztuo.bitrade.dao.MemberLoginRecordDao;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberLoginRecord;
import cn.ztuo.bitrade.entity.QMemberLoginRecord;
import cn.ztuo.bitrade.pagination.PageResult;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.DateUtil;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class MemberLoginRecordService extends BaseService {

    @Autowired
    private MemberLoginRecordDao memberLoginRecordDao;


    public void saveLoginRecord(Member member, LoginStatus status,String ip,String way) {
        MemberLoginRecord memberLoginRecord = new MemberLoginRecord();
        memberLoginRecord.setMemberId(member.getId());
        memberLoginRecord.setStatus(status);
        memberLoginRecord.setIp(ip);
        memberLoginRecord.setWay(way);
        memberLoginRecord.setLoginTime(DateUtil.getCurrentDate());
        memberLoginRecordDao.saveAndFlush(memberLoginRecord);
    }

    @Transactional(readOnly = true)
    public PageResult<MemberLoginRecord> query(Long memberId,Integer pageNo, Integer pageSize) {
        List<MemberLoginRecord> list;
        JPAQuery<MemberLoginRecord> jpaQuery = queryFactory.selectFrom(QMemberLoginRecord.memberLoginRecord);
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        booleanExpressionList.add(QMemberLoginRecord.memberLoginRecord.memberId.eq(memberId));
        jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        jpaQuery.orderBy(QMemberLoginRecord.memberLoginRecord.loginTime.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }
}
