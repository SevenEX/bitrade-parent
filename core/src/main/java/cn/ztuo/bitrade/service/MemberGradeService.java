package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.dao.MemberGradeDao;
import cn.ztuo.bitrade.entity.MemberGrade;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description: MemberGradeService
 * @author: MrGao
 * @create: 2019/04/25 15:53
 */
@Service
public class MemberGradeService extends BaseService<MemberGrade> {

    @Autowired
    private MemberGradeDao dao;
    @Autowired
    private RedisUtil redisUtil ;

    /**
     * 查询所有
     * @return
     */
    @Override
    public List<MemberGrade> findAll(){
        return dao.findAll();
    }

    /**
     * 根据id查询单个
     * @param id
     * @return
     */
    public MemberGrade findOne(Long id) {
//        Object object = redisUtil.get(SysConstant.CUSTOMER_INTEGRATION_GRADE+id);
//        MemberGrade memberGrade ;
//        if(object==null){
//            memberGrade = dao.findOne(id);
//            redisUtil.set(SysConstant.CUSTOMER_INTEGRATION_GRADE+id,memberGrade);
//        }else {
//            memberGrade = (MemberGrade)object;
//        }
        return dao.findById(id).orElse(null);
    }

    /**
     * 保存
     * @param memberGrade
     * @return
     */
    public MemberGrade save(MemberGrade memberGrade){
        if(memberGrade.getId()!=null){
            redisUtil.delete(SysConstant.CUSTOMER_INTEGRATION_GRADE+memberGrade.getId());
        }
        return dao.save(memberGrade);
    }

    public int updateMemberGrade(BigDecimal exchangeFeeRate, BigDecimal exchangeMakerFeeRate){
        return dao.updateMemberGrade( exchangeFeeRate, exchangeMakerFeeRate);
    }

    public int updateOtcFee(BigDecimal otcFeeRate){
        return dao.updateOtcFee( otcFeeRate );
    }

    public int updateSeDiscountRate(BigDecimal seDiscountRate){
        return dao.updateSeDiscountRate( seDiscountRate );
    }



}
