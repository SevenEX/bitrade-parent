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
public class IeoEmption implements Serializable{	

	private static final long serialVersionUID = 1L;

    /**
     * 主键ID自增
     * This field corresponds to the database column ieo_emption.ID
     *
     * @date 2019-04-26 15:22:41
     */
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
	private Long id;
	
    /**
     * 项目名称
     * This field corresponds to the database column ieo_emption.ieo_name
     *
     * @date 2019-04-26 15:22:41
     */  
	private String ieoName;
	
    /**
     * 项目缩略图
     * This field corresponds to the database column ieo_emption.pic_view
     *
     * @date 2019-04-26 15:22:41
     */  
	private String picView;
	
    /**
     * 项目图片
     * This field corresponds to the database column ieo_emption.pic
     *
     * @date 2019-04-26 15:22:41
     */  
	private String pic;
	
    /**
     * 发售币种
     * This field corresponds to the database column ieo_emption.sale_coin
     *
     * @date 2019-04-26 15:22:41
     */  
	private String saleCoin;
	
    /**
     * 发售总量
     * This field corresponds to the database column ieo_emption.sale_amount
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal saleAmount;
	
    /**
     * 募集币种
     * This field corresponds to the database column ieo_emption.raise_coin
     *
     * @date 2019-04-26 15:22:41
     */  
	private String raiseCoin;
	
    /**
     * 募集币种与发售币种的比率
     * This field corresponds to the database column ieo_emption.ratio
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal ratio;
	
    /**
     * 募集开始时间
     * This field corresponds to the database column ieo_emption.start_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date startTime;
	
    /**
     * 募集结束时间
     * This field corresponds to the database column ieo_emption.end_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date endTime;
	
    /**
     * 手续费
     * This field corresponds to the database column ieo_emption.fee
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal fee;
	
    /**
     * 预计上线时间
     * This field corresponds to the database column ieo_emption.expect_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date expectTime;
	
    /**
     * 抢购成功几率
     * This field corresponds to the database column ieo_emption.success_ratio
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal successRatio;
	
    /**
     * 每人抢购限额
     * This field corresponds to the database column ieo_emption.limit_amount
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal limitAmount;
	
    /**
     * 允许抢购条件持有数量
     * This field corresponds to the database column ieo_emption.have_amount
     *
     * @date 2019-04-26 15:22:41
     */  
	private BigDecimal haveAmount;
	
    /**
     * 持有币种
     * This field corresponds to the database column ieo_emption.have_coin
     *
     * @date 2019-04-26 15:22:41
     */  
	private String haveCoin;


    /**
     * 剩余量
     */
	private BigDecimal surplusAmount;
    /**
     * 售卖方式
     * This field corresponds to the database column ieo_emption.sell_mode
     *
     * @date 2019-04-26 15:22:41
     */  
	private String sellMode;
	
    /**
     * 项目详情
     * This field corresponds to the database column ieo_emption.sell_detail
     *
     * @date 2019-04-26 15:22:41
     */  
	private String sellDetail;
	
    /**
     * This field corresponds to the database column ieo_emption.create_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date createTime;
	
    /**
     * This field corresponds to the database column ieo_emption.create_user
     *
     * @date 2019-04-26 15:22:41
     */  
	private String createUser;
	
    /**
     * This field corresponds to the database column ieo_emption.update_time
     *
     * @date 2019-04-26 15:22:41
     */  
	private Date updateTime;
	
    /**
     * This field corresponds to the database column ieo_emption.update_user
     *
     * @date 2019-04-26 15:22:41
     */  
	private String updateUser;
}