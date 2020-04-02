package cn.ztuo.bitrade.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

import java.math.BigDecimal;  
import java.util.Date;  



/**
 * @author gs
 */
@Entity
@Data
public class GiftConfig implements Serializable{

	private static final long serialVersionUID = 1L;

    /**
     * 主键ID自增
     * This field corresponds to the database column gift_config.ID
     *
     * @date 2019-04-29 09:43:49
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
	private Long id;
	
    /**
     * 活动名称
     * This field corresponds to the database column gift_config.gift_name
     *
     * @date 2019-04-29 09:43:49
     */  
	private String giftName;
	
    /**
     * 赠送币种
     * This field corresponds to the database column gift_config.gift_coin
     *
     * @date 2019-04-29 09:43:49
     */  
	private String giftCoin;
	
    /**
     * 总量
     * This field corresponds to the database column gift_config.amount
     *
     * @date 2019-04-29 09:43:49
     */  
	private BigDecimal amount;
	
    /**
     * 持有币种
     * This field corresponds to the database column gift_config.have_coin
     *
     * @date 2019-04-29 09:43:49
     */  
	private String haveCoin;
	
    /**
     * 持有数量
     * This field corresponds to the database column gift_config.have_amount
     *
     * @date 2019-04-29 09:43:49
     */  
	private BigDecimal haveAmount;
	
    /**
     * This field corresponds to the database column gift_config.create_time
     *
     * @date 2019-04-29 09:43:49
     */  
	private Date createTime;

}