package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.enums.PerformActionsEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 风险记录
 */
@Entity
@Data
@Table
public class RiskRecord {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lever_coin_id")
    private LeverCoin leverCoin;

    @ManyToOne
    @JoinColumn(name="loss_threshold_id")
    private LossThreshold lossThreshold;

    private Long memberId;

    private String memberName;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 当时风险率
     */
    private BigDecimal currentThreshold;

    /**
     * 执行的动作
     */
    @Enumerated(EnumType.ORDINAL)
    private PerformActionsEnum performActions;

    public RiskRecord(LeverCoin leverCoin,LossThreshold lossThreshold,Long memberId,String memberName,BigDecimal currentThreshold,
                      PerformActionsEnum performActions){
        this.leverCoin=leverCoin;
        this.lossThreshold=lossThreshold;
        this.memberId=memberId;
        this.currentThreshold=currentThreshold;
        this.memberName=memberName;
        this.performActions=performActions;
        createTime=new Date();
    }
}
