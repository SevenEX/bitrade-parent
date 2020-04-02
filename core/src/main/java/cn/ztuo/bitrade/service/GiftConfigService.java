package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.GiftConfigDao;
import cn.ztuo.bitrade.entity.GiftConfig;
import cn.ztuo.bitrade.entity.QGiftConfig;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.PredicateUtils;
import cn.ztuo.bitrade.vo.GiftConfigVO;
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
 * @Date: 2019/4/29 11:13 AM
 */
@Service
public class GiftConfigService extends BaseService {

    @Autowired
    private GiftConfigDao giftConfigDao;

    public GiftConfig save(GiftConfig giftRecord){
        return giftConfigDao.save(giftRecord);
    }

    public GiftConfig findById(Long id){
        return giftConfigDao.findById(id).orElse(null);
    }


    public Page<GiftConfig> getByPage(GiftConfigVO giftConfigVO) throws Exception{
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotEmpty(giftConfigVO.getStartTime())){
            booleanExpressions.add(QGiftConfig.giftConfig.createTime.goe(DateUtil.stringToDate(giftConfigVO
                    .getStartTime())));
        }
        if (StringUtils.isNotEmpty(giftConfigVO.getEndTime())){
            booleanExpressions.add(QGiftConfig.giftConfig.createTime.loe(DateUtil.stringToDate(giftConfigVO
                    .getEndTime())));
        }
        if (StringUtils.isNotEmpty(giftConfigVO.getGiftName())){
            booleanExpressions.add(QGiftConfig.giftConfig.giftName.loe("%"+giftConfigVO.getGiftName()+"%"));
        }
        if (giftConfigVO.getId() != null){
            booleanExpressions.add(QGiftConfig.giftConfig.id.eq(giftConfigVO.getId()));
        }
        Predicate predicate = PredicateUtils.getPredicate(booleanExpressions);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(giftConfigVO.getPageNum() - 1, giftConfigVO.getPageSize(), sort);
        return giftConfigDao.findAll(predicate,pageable);
    }


}
