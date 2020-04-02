package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.IntegrationRecordDao;
import cn.ztuo.bitrade.entity.IntegrationRecord;
import cn.ztuo.bitrade.entity.QIntegrationRecord;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.IntegrationRecordVO;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @description: IntegrationRecordService
 * @author: MrGao
 * @create: 2019/04/25 19:08
 */
@Service
public class IntegrationRecordService extends BaseService<IntegrationRecord> {

    @Autowired
    private IntegrationRecordDao dao ;

    public Page<IntegrationRecord> findRecord4Page(IntegrationRecordVO queryVo) {
        QIntegrationRecord qIntegrationRecord = QIntegrationRecord.integrationRecord;
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if(queryVo.getUserId()!=null){
            booleanExpressions.add(qIntegrationRecord.memberId.eq(queryVo.getUserId()));
        }
        if(queryVo.getType()!=null){
            booleanExpressions.add(qIntegrationRecord.type.eq(queryVo.getType()));
        }
        if(StringUtils.isNotEmpty(queryVo.getCreateStartTime())){
            booleanExpressions.add(qIntegrationRecord.createTime.gt(DateUtil.strToDate(queryVo.getCreateStartTime())));
        }
        if(StringUtils.isNotEmpty(queryVo.getCreateEndTime())){
            booleanExpressions.add(qIntegrationRecord.createTime.lt(DateUtil.strToDate(queryVo.getCreateEndTime())));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(queryVo.getPageNum()-1,queryVo.getPageSize(),sort);
        return dao.findAll(predicate,pageable);
    }


    public IntegrationRecord save(IntegrationRecord record){
        return dao.save(record);
    }
}
