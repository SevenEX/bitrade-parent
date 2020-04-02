package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.IeoEmption;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @Description:
 * @Author: Seven
 * @Date: 2019/4/26 3:58 PM
 */
public interface IeoEmptionDao extends BaseDao<IeoEmption> {


    @Query(value = "SELECT * from ieo_emption where id=:id and start_time<=:times AND end_time>:times",nativeQuery = true)
    IeoEmption findbyCondition(@Param("id") Long id,@Param("times") String times);

    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "UPDATE ieo_emption set surplus_amount=surplus_amount-:receAmount where id=:id AND " +
            "surplus_amount>=:receAmount",nativeQuery = true)
    int subAmount(@Param("id") Long id, @Param("receAmount") BigDecimal receAmount);
}
