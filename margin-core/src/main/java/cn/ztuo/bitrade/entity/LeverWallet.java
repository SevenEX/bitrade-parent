package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.enums.WalletEnum;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 杠杆交易钱包
 * @author zhang yingxin
 * @date 2018/5/25
 */
@Entity
@Data
@Table
public class LeverWallet {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;
    private String memberName;
    private String mobilePhone ;
    private String email ;

    @ManyToOne
    @JoinColumn(name = "lever_coin_id")
    private LeverCoin leverCoin;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;
    /**
     * 可用余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '可用余额'")
    private BigDecimal balance=BigDecimal.ZERO;
    /**
     * 冻结余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '冻结余额'")
    private BigDecimal frozenBalance=BigDecimal.ZERO;

    @JsonIgnore
    @Version
    private int version;

    /**
     * 钱包是否锁定，0否，1是
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '钱包是否锁定'")
    private BooleanEnum isLock = BooleanEnum.IS_FALSE;

    /**
     * 钱包状态，0正常，1爆仓，2平仓，只有钱包锁定时才看这个状态
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '是否处于爆仓状态'")
    private WalletEnum status=WalletEnum.NORMAL;

    /**
     * 钱包余额折合BTC
     */
    @Transient
    private BigDecimal foldBtc;
}
