package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.GiftRecordDao;
import cn.ztuo.bitrade.entity.GiftRecord;
import cn.ztuo.bitrade.entity.QGiftRecord;
import cn.ztuo.bitrade.service.Base.BaseService;

import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.GiftRecordVO;
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
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/29 11:11 AM
 */
@Service
public class GiftRecordService extends BaseService {
    
    @Autowired
    private GiftRecordDao giftRecordDao;


    public GiftRecord save(GiftRecord giftRecord){
        return giftRecordDao.save(giftRecord);
    }

    public GiftRecord findById(Long id){
        return giftRecordDao.findById(id).orElse(null);
    }
    
    public Page<GiftRecord> getByPage(GiftRecordVO giftRecordVO) throws Exception{
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotEmpty(giftRecordVO.getStartTime())){
            booleanExpressions.add(QGiftRecord.giftRecord.createTime.goe(DateUtil.stringToDate(giftRecordVO
                    .getStartTime())));
        }
        if (StringUtils.isNotEmpty(giftRecordVO.getEndTime())){
            booleanExpressions.add(QGiftRecord.giftRecord.createTime.loe(DateUtil.stringToDate(giftRecordVO
                    .getEndTime())));
        }
        if (StringUtils.isNotEmpty(giftRecordVO.getUserName())){
            booleanExpressions.add(QGiftRecord.giftRecord.userName.loe("%"+giftRecordVO.getUserName()+"%"));
        }
        if (StringUtils.isNotEmpty(giftRecordVO.getMobile())){
            booleanExpressions.add(QGiftRecord.giftRecord.userMobile.loe(giftRecordVO.getMobile()));
        }
        if (giftRecordVO.getUserId() != null){
            booleanExpressions.add(QGiftRecord.giftRecord.userId.eq(giftRecordVO.getUserId()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(giftRecordVO.getPageNum() - 1, giftRecordVO.getPageSize(), sort);
        return giftRecordDao.findAll(predicate,pageable);
    }
}
