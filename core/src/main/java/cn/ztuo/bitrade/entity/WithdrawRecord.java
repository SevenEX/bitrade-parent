package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.WithdrawStatus;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提币申请
 *
 * @author Seven
 * @date 2019年01月29日
 */
@Entity
@Data
public class WithdrawRecord {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    /**
     * 申请人id
     */
    private Long memberId;
    /**
     * 币种
     */
    @JoinColumn(name = "coin_id", nullable = false)
    @ManyToOne
    private Coin coin;

    private String coinKey;
    /**
     * 申请总数量
     */
    @Column(columnDefinition = "decimal(20,8) comment '申请总数量'")
    private BigDecimal totalAmount;
    /**
     * 手续费
     */
    @Column(columnDefinition = "decimal(20,8) comment '手续费'")
    private BigDecimal fee;
    /**
     * 预计到账数量
     */
    @Column(columnDefinition = "decimal(20,8) comment '预计到账数量'")
    private BigDecimal arrivedAmount;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealTime;
    /**
     * 提现状态
     */
    @Enumerated(EnumType.ORDINAL)
    private WithdrawStatus status = WithdrawStatus.PROCESSING;
    /**
     * 是否是自动提现
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isAuto;
    /**
     * 审核人
     */
    @JoinColumn(name = "admin_id")
    @ManyToOne
    private Admin admin;
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum canAutoWithdraw;

    /**
     * 交易编号
     */
    private String transactionNumber;
    /**
     * 提现地址
     */
    private String address;

    private String remark;

    /**
     * 是否是快速提币
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum isQuick;
}
