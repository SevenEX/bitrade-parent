package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Seven
 * @date 2019年03月20日
 */
@Data
@Builder
public class PromotionRewardStatistics {
    private BigDecimal amount;
    private String createTime;
    private String orderMember;

}
