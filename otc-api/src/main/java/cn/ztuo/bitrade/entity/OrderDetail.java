package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.AdvertiseType;
import cn.ztuo.bitrade.constant.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Seven
 * @date 2019年01月20日
 */
@Builder
@Data
public class OrderDetail {
    private String orderSn;
    private AdvertiseType type;
    private String unit;
    private OrderStatus status;
    private BigDecimal price;
    private BigDecimal money;
    private BigDecimal amount;
    private BigDecimal commission;
    private PayInfo payInfo;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Long createTime;
    private Date payTime;
    private int timeLimit;
    private String otherSide;
    private long myId;
    private long hisId;
    private String referenceNumber;
    private String remark;
    private Appeal appeal;
    private String payMode;
    private Long serverTime;
}
