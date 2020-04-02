package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.VerifyType;
import cn.ztuo.bitrade.dao.MemberVerifyRecordDao;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberVerifyRecord;
import cn.ztuo.bitrade.entity.QMemberVerifyRecord;
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
public class MemberVerifyRecordService extends BaseService {

    @Autowired
    private MemberVerifyRecordDao memberVerifyRecordDao;


    public void saveVerifyRecord(Member member, VerifyType type, String ip) {
        MemberVerifyRecord memberVerifyRecord = new MemberVerifyRecord();
        memberVerifyRecord.setMemberId(member.getId());
        memberVerifyRecord.setType(type);
        memberVerifyRecord.setIp(ip);
        memberVerifyRecord.setCreateTime(DateUtil.getCurrentDate());
        memberVerifyRecordDao.saveAndFlush(memberVerifyRecord);
    }

    @Transactional(readOnly = true)
    public PageResult<MemberVerifyRecord> query(Long memberId,Integer pageNo, Integer pageSize) {
        List<MemberVerifyRecord> list;
        JPAQuery<MemberVerifyRecord> jpaQuery = queryFactory.selectFrom(QMemberVerifyRecord.memberVerifyRecord);
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        booleanExpressionList.add(QMemberVerifyRecord.memberVerifyRecord.memberId.eq(memberId));
        jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        jpaQuery.orderBy(QMemberVerifyRecord.memberVerifyRecord.createTime.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }
}
