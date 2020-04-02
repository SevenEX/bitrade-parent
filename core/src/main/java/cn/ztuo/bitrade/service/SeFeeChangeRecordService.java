package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.SeFeeChangeStatus;
import cn.ztuo.bitrade.constant.SeFeeChangeType;
import cn.ztuo.bitrade.constant.SeFeeChangeWay;
import cn.ztuo.bitrade.dao.SeFeeChangeRecordDao;
import cn.ztuo.bitrade.entity.SeFeeChangeRecord;
import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.QSeFeeChangeRecord;
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
public class SeFeeChangeRecordService extends BaseService {

    @Autowired
    private SeFeeChangeRecordDao seFeeChangeRecordDao;


    public void saveSeFeeChangeRecord(Member member, SeFeeChangeType type, SeFeeChangeWay way) {
        seFeeChangeRecordDao.updateStatus(member.getId());
        SeFeeChangeRecord seFeeChangeRecord = new SeFeeChangeRecord();
        seFeeChangeRecord.setMemberId(member.getId());
        seFeeChangeRecord.setType(type);
        seFeeChangeRecord.setWay(way);
        seFeeChangeRecord.setStatus(SeFeeChangeStatus.INFORCE);
        seFeeChangeRecord.setCreateTime(DateUtil.getCurrentDate());
        seFeeChangeRecordDao.saveAndFlush(seFeeChangeRecord);
    }

    @Transactional(readOnly = true)
    public PageResult<SeFeeChangeRecord> query(Long memberId, Integer pageNo, Integer pageSize) {
        List<SeFeeChangeRecord> list;
        JPAQuery<SeFeeChangeRecord> jpaQuery = queryFactory.selectFrom(QSeFeeChangeRecord.seFeeChangeRecord);
        List<BooleanExpression> booleanExpressionList = new ArrayList();
        booleanExpressionList.add(QSeFeeChangeRecord.seFeeChangeRecord.memberId.eq(memberId));
        jpaQuery.where(booleanExpressionList.toArray(new BooleanExpression[booleanExpressionList.size()]));
        jpaQuery.orderBy(QSeFeeChangeRecord.seFeeChangeRecord.createTime.desc());
        if (pageNo != null && pageSize != null) {
            list = jpaQuery.offset((pageNo - 1) * pageSize).limit(pageSize).fetch();
        } else {
            list = jpaQuery.fetch();
        }
        return new PageResult<>(list, jpaQuery.fetchCount());
    }
}
