package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author MrGao
 * @description 会员钱包
 * @date 2018/1/2 15:28
 */
@Entity
@Data
@Table(uniqueConstraints ={@UniqueConstraint(columnNames={"memberId", "coin_id"})})
public class MemberWallet {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;

    private transient List<String> exchangeCoinList;

    private transient List<MemberWalletRelation> addressList;

    private transient boolean canOtc = false;

    /**
     * 可用余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '可用余额'")
    private BigDecimal balance;
    /**
     * 冻结余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '冻结余额'")
    private BigDecimal frozenBalance;

    /**
     * 充值地址
     */
    private String address;

    @JsonIgnore
    @Version
    private int version;

    /**
     * 钱包是否锁定，0否，1是。锁定后
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(columnDefinition = "int default 0 comment '钱包不是锁定'")
    private BooleanEnum isLock = BooleanEnum.IS_FALSE;

    /**
     * 释放余额
     */
    @Column(columnDefinition = "decimal(20,8) comment '释放余额'")
    private BigDecimal releaseBalance;
}
