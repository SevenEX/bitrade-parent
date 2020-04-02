package cn.ztuo.bitrade.service;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cn.ztuo.bitrade.dao.EmptionRecordDao;
import cn.ztuo.bitrade.entity.EmptionRecord;
import cn.ztuo.bitrade.entity.QEmptionRecord;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.EmptionRecrodVO;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/26 4:00 PM
 */
@Service
public class EmptionRecordService extends BaseService{

    @Autowired
    private EmptionRecordDao emptionRecordDao;


    public EmptionRecord save(EmptionRecord emptionRecord){
        return emptionRecordDao.save(emptionRecord);
    }

    public Page<EmptionRecord> getByPage(EmptionRecrodVO emptionRecrodVO) throws Exception{
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotEmpty(emptionRecrodVO.getStartTime())){
            booleanExpressions.add(QEmptionRecord.emptionRecord.createTime.goe(DateUtil.stringToDate(emptionRecrodVO
                    .getStartTime())));
        }
        if (StringUtils.isNotEmpty(emptionRecrodVO.getEndTime())){
            booleanExpressions.add(QEmptionRecord.emptionRecord.createTime.loe(DateUtil.stringToDate(emptionRecrodVO
                    .getEndTime())));
        }
        if (StringUtils.isNotEmpty(emptionRecrodVO.getIeoName())){
            booleanExpressions.add(QEmptionRecord.emptionRecord.ieoName.like("%"+emptionRecrodVO.getIeoName()+"%"));
        }
        if (StringUtils.isNotEmpty(emptionRecrodVO.getUserName())){
            booleanExpressions.add(QEmptionRecord.emptionRecord.userName.like("%"+emptionRecrodVO.getUserName()+"%"));
        }
        if (StringUtils.isNotEmpty(emptionRecrodVO.getUserMobile())){
            booleanExpressions.add(QEmptionRecord.emptionRecord.userMobile.eq(emptionRecrodVO.getUserMobile()));
        }
        if (StringUtils.isNotEmpty(emptionRecrodVO.getStatus())){
            booleanExpressions.add(QEmptionRecord.emptionRecord.status.eq(emptionRecrodVO.getStatus()));
        }
        if (emptionRecrodVO.getUserId() != null){
            booleanExpressions.add(QEmptionRecord.emptionRecord.userId.eq(emptionRecrodVO.getUserId()));

        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(emptionRecrodVO.getPageNum() - 1, emptionRecrodVO.getPageSize(), sort);
        return emptionRecordDao.findAll(predicate,pageable);

    }

    public EmptionRecord findById(Long id){
        return emptionRecordDao.findById(id).orElse(null);
    }

}
