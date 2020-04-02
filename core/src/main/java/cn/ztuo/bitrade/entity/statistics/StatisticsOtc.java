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
@ApiModel(value = "数据统计-法币交易")
@Data
@Entity
@Table(name = "statistics_otc")
public class StatisticsOtc {
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
     * 交易对
     */
    @ApiModelProperty(value = "币种")
    private String unit;

    /**
     * 交易量
     */
    @ApiModelProperty(value = "交易量")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易额")
    private BigDecimal money;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    public StatisticsOtc() {
    }

    public StatisticsOtc(Date date, String unit) {
        this.date = date;
        this.unit = unit;
        this.amount = BigDecimal.ZERO;
        this.money = BigDecimal.ZERO;
        this.fee = BigDecimal.ZERO;
    }
}
