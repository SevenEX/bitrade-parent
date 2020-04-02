package cn.ztuo.bitrade.entity.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Paradise
 */
@Entity
@ApiModel(value = "数据统计-注册统计")
@Data
@Table(name = "statistics_register")
public class StatisticsRegister {
    /**
     * 自增主键
     */
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @ApiModelProperty(value = "自增主键")
    private Long id;

    /**
     * 日期字符串
     */
    @ApiModelProperty(value = "日期字符串")
    @Column(name = "date_", nullable = false)
    private String date;

    /**
     * 注册总数
     */
    @ApiModelProperty(value = "注册总数")
    private Integer totalCount;

    /**
     * 自助注册总数
     */
    @ApiModelProperty(value = "自助注册总数")
    private Integer selfCount;

    /**
     * 邀请注册总数
     */
    @ApiModelProperty(value = "邀请注册总数")
    private Integer invitedCount;

    /**
     * 间接邀请注册总数
     */
    @ApiModelProperty(value = "间接邀请注册总数")
    private Integer indirectCount;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
