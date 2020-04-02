package cn.ztuo.bitrade.entity.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Paradise
 */
@Entity
@ApiModel(value = "数据统计-充币统计")
@Data
@Table(name = "statistics_recharge")
public class StatisticsRecharge {
    /**
     * 自增主键
     */
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @ApiModelProperty(value = "自增主键")
    private Long id;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    @Column(name = "date_", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 充币人数
     */
    @ApiModelProperty(value = "充币人数")
    private Integer peopleCount;

    /**
     * 充币笔数
     */
    @ApiModelProperty(value = "充币笔数")
    private Integer rechargeCount;

    /**
     * 充值金额
     */
    @ApiModelProperty(value = "充值金额")
    private BigDecimal rechargeAmount;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}

