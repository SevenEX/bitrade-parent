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
public class GiftRecord implements Serializable{	

	private static final long serialVersionUID = 1L;

    /**
     * 主键ID自增
     * This field corresponds to the database column gift_record.ID
     *
     * @date 2019-04-29 09:43:49
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
	private Long id;
	
    /**
     * 用户ID
     * This field corresponds to the database column gift_record.user_id
     *
     * @date 2019-04-29 09:43:49
     */  
	private Long userId;
	
    /**
     * 用户昵称
     * This field corresponds to the database column gift_record.user_name
     *
     * @date 2019-04-29 09:43:49
     */  
	private String userName;
	
    /**
     * 用户手机号
     * This field corresponds to the database column gift_record.user_mobile
     *
     * @date 2019-04-29 09:43:49
     */  
	private String userMobile;
	
    /**
     * 活动名称
     * This field corresponds to the database column gift_record.gift_name
     *
     * @date 2019-04-29 09:43:49
     */  
	private String giftName;
	
    /**
     * 赠送币种
     * This field corresponds to the database column gift_record.gift_coin
     *
     * @date 2019-04-29 09:43:49
     */  
	private String giftCoin;
	
    /**
     * 赠送数量
     * This field corresponds to the database column gift_record.gift_amount
     *
     * @date 2019-04-29 09:43:49
     */  
	private BigDecimal giftAmount;
	
    /**
     * This field corresponds to the database column gift_record.create_time
     *
     * @date 2019-04-29 09:43:49
     */  
	private Date createTime;
}