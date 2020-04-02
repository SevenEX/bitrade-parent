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
@ApiModel(value = "数据统计-手续费")
@Data
@Entity
@Table(name = "statistics_service_fee")
public class StatisticsServiceFee {
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    @Column(name = "currency", nullable = false)
    private String currency;

    /**
     * 币币交易手续费
     */
    @ApiModelProperty(value = "币币交易手续费")
    private BigDecimal coinFee;

    /**
     * 法币交易手续费
     */
    @ApiModelProperty(value = "法币交易手续费")
    private BigDecimal legalFee;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}

