package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @description: MemberGrade
 * 会员等级设置
 * @author: MrGao
 * @create: 2019/04/25 15:41
 */
@Entity
@Data
public class MemberGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 等级名称
     */
    private String gradeName ;
    /**
     * 等级code
     */
    private String gradeCode ;
    /**
     * 每日提币数量限制
     */
    private BigDecimal withdrawCoinAmount ;
    /**
     * 每日提币笔数
     */
    private Integer dayWithdrawCount ;
    /**
     * 吃单手续费比例
     */
    private BigDecimal exchangeFeeRate ;
    /**
     * 等级界限
     */
    private Integer gradeBound;
    /**
     * 挂单手续费比例
     */
    private BigDecimal exchangeMakerFeeRate;

    /**
     * 商家手续费比例
     */
    private BigDecimal otcFeeRate;

    /**
     * SE抵扣折扣率
     */
    private BigDecimal seDiscountRate;

}
