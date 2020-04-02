package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 分红的USDT钱包
 */
@Entity
@Data
public class RewardWallet {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;
    /**
     * 币种单位
     */
    private String coinUnit;
    /**
     * 可用余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '可用余额'")
    private BigDecimal balance;

    @JsonIgnore
    @Version
    private int version;

    /**
     * 冻结余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '冻结余额'")
    private BigDecimal frozenBalance;

    /**
     * 钱包是否锁定，0否，1是。锁定后
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '钱包不是锁定'")
    private BooleanEnum isLock = BooleanEnum.IS_FALSE;
}
