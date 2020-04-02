package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 *锁仓记录表
 */
@Entity
@Data
@Table
public class LockPositionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String memberName;

    @OneToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    /**
     * 锁仓时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 锁仓状态，0锁定，1解锁
     */
    private CommonStatus status;

    /**
     * 解锁时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date unlockTime;

    /**
     * 锁仓原因
     */
    private String reason;

    @Column(columnDefinition = "decimal(20,8) comment '锁仓金额'")
    private BigDecimal amount;

    private Long walletId;
}
