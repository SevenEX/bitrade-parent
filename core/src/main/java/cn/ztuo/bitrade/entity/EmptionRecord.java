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
public class EmptionRecord implements Serializable{	

	private static final long serialVersionUID = 1L;

    /**
     * 主键ID自增
     * This field corresponds to the database column emption_record.ID
     *
     * @date 2019-04-26 15:22:41
     */
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
	private Long id;
	
    /**
     * 用户ID
     * This field corresponds to the database column emption_record.user_id
     *
     * @date 2019-04-26 15:22:41
     */  
	private Long userId;

	private String userName;

	private String userMobile;

	/**
	 * 项目缩略图
	 * This field corresponds to the database column ieo_emption.pic_view
	 *
	 * @date 2019-04-26 15:22:41
	 */
	private String picView;
    /**
     * 项目名称
     * This field corresponds to the database column emption_record.ieo_name
     *
     * @date 2019-04-26 15:22:41
     */  
	private String ieoName;

	private Long ieoId;
	
    /**
     * 发售币种
     * This field corresponds to the database column emption_record.sale_coin
     *
     * @date 2019-04-26 15:22:41
     */  
	private String saleCoin;
	
    /**
     * 发售总量
     * This field corresponds to the database column emption_record.sale_amount
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal saleAmount;
	
    /**
     * 募集币种
     * This field corresponds to the database column emption_record.raise_coin
     *
     * @date 2019-04-26 15:22:41
     */  
	private String raiseCoin;
	
    /**
     * 募集币种与发售币种的比率
     * This field corresponds to the database column emption_record.ratio
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal ratio;
	
    /**
     * 募集开始时间
     * This field corresponds to the database column emption_record.start_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date startTime;
	
    /**
     * 募集结束时间
     * This field corresponds to the database column emption_record.end_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date endTime;
	
    /**
     * 状态 (0-失败，1-成功)
     * This field corresponds to the database column emption_record.status
     *
     * @date 2019-04-26 15:22:41
     */  
	private String status;
	
    /**
     * 认购数量
     * This field corresponds to the database column emption_record.receive_amount
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal receiveAmount;
	
    /**
     * 使用数量
     * This field corresponds to the database column emption_record.pay_amount
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal payAmount;
	
    /**
     * 预计上线时间
     * This field corresponds to the database column emption_record.expect_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date expectTime;
	
    /**
     * This field corresponds to the database column emption_record.create_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date createTime;

}