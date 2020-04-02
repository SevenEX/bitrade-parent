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
@ApiModel(value = "数据统计-交易统计")
@Data
@Entity
@Table(name = "statistics_transaction")
public class StatisticsTransaction {
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
    @ApiModelProperty(value = "交易对")
    private String symbol;

    /**
     * 交易人数
     */
    @ApiModelProperty(value = "交易人数")
    private Integer peopleCount;

    /**
     * 买入人数
     */
    @ApiModelProperty(value = "买入人数")
    private Integer purchasePeopleCount;

    /**
     * 卖出人数
     */
    @ApiModelProperty(value = "卖出人数")
    private Integer sellPeopleCount;

    /**
     * 交易笔数
     */
    @ApiModelProperty(value = "交易笔数")
    private Integer transactionCount;

    /**
     * 买入笔数
     */
    @ApiModelProperty(value = "买入笔数")
    private Integer txPurchaseCount;

    /**
     * 卖出笔数
     */
    @ApiModelProperty(value = "卖出笔数")
    private Integer txSellCount;

    /**
     * 交易量
     */
    @ApiModelProperty(value = "交易量")
    private BigDecimal transactionAmount;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
