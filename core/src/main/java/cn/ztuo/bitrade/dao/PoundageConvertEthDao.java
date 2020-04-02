package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.PoundageConvertEth;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PoundageConvertEthDao  extends BaseDao<PoundageConvertEth> {
    /**
     * 根据时间查询以太坊手续费
     * 首页展示用的数据 机器人账户不排除
     * @param startDate
     * @param endDate
     * @return
     */
    @Query(value = "SELECT SUM(poundage_amount_Eth) poundage_amount_eth FROM poundage_convert_eth WHERE transaction_time>= :startTime AND transaction_time<=:endTime ",nativeQuery = true)
    BigDecimal getAmountEthByDate(@Param("startTime") String startDate, @Param("endTime") String endDate);

    /**
     * 根据时间查询以太坊手续费
     * 持币分红 排除机器人账户
     * @param startTime
     * @param endTime
     * @return
     */
    @Query(value = "SELECT SUM(poundage_amount_Eth) poundage_amount_eth FROM poundage_convert_eth WHERE transaction_time>= :startTime AND transaction_time<=:endTime AND member_id NOT IN (66946,65859,52350,118863,119284,76895)",nativeQuery = true)
    BigDecimal getAmountEth(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
