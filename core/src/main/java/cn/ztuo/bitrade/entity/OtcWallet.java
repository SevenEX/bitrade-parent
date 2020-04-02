package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

import java.math.BigDecimal;

/**
 * @author gs
 */
@Entity
@Data
public class OtcWallet implements Serializable{	

	private static final long serialVersionUID = 1L;

    /**
     * This field corresponds to the database column otc_wallet.id
     *
     * @date 2019-05-05 15:21:32
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
	private Long id;
	
    /**
     * 可用余额
     * This field corresponds to the database column otc_wallet.balance
     *
     * @date 2019-05-05 15:21:32
     */
    @Column(columnDefinition = "decimal(20,8) comment '可用余额'")
	private BigDecimal balance;
	
    /**
     * 冻结余额
     * This field corresponds to the database column otc_wallet.frozen_balance
     *
     * @date 2019-05-05 15:21:32
     */
    @Column(columnDefinition = "decimal(20,8) comment '冻结余额'")
	private BigDecimal frozenBalance;
	
    /**
     * 待释放余额
     * This field corresponds to the database column otc_wallet.release_balance
     *
     * @date 2019-05-05 15:21:32
     */
    @Column(columnDefinition = "decimal(20,8) comment '待释放余额'")
	private BigDecimal releaseBalance;
	
    /**
     * 钱包不是锁定（0-未锁定，1-已锁定）
     * This field corresponds to the database column otc_wallet.is_lock
     *
     * @date 2019-05-05 15:21:32
     */  
	private Integer isLock;
	
    /**
     * This field corresponds to the database column otc_wallet.member_id
     *
     * @date 2019-05-05 15:21:32
     */  
	private Long memberId;
	
    /**
     * This field corresponds to the database column otc_wallet.version
     *
     * @date 2019-05-05 15:21:32
     */  
	private Integer version;
	
    /**
     * This field corresponds to the database column otc_wallet.coin_id
     *
     * @date 2019-05-05 15:21:32
     */  
//	private String coinId;


    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;


}