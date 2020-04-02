package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.AdvertiseType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderScreen extends OtcOrderTopScreen{
    private String orderSn;
    private String payModel;
    private BigDecimal minNumber ;
    private BigDecimal maxNumber ;
    //private String memberName;//用户名和真名的关键字即可
    //private String customerName;//用户名和真名的关键字即可
    private BigDecimal minMoney;
    private BigDecimal maxMoney;
    private AdvertiseType advertiseType ;
}
