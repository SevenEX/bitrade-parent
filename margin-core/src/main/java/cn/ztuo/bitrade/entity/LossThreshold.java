package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.enums.PerformActionsEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 亏损阈值
 */
@Entity
@Data
@Table
public class LossThreshold {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private LeverCoin leverCoin;

    /**
     * 阈值，钱包余额除以借贷金额的值
     */
    private BigDecimal threshold;

    /**
     * 执行的动作
     */
    @Enumerated(EnumType.ORDINAL)
    private PerformActionsEnum performActions;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;
}
