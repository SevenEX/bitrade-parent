package cn.ztuo.bitrade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.WithdrawStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class WithdrawRecordVO {

    private Long id ;
    private Long memberId ;
    private String memberUsername ;
    private String memberRealName ;
    private String phone ;
    private String email ;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dealTime ;
    private String unit ;
    private BigDecimal totalAmount ;
    private BigDecimal fee ;
    private BigDecimal arrivedAmount ;
    private String transactionNumber ;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime ;
    private String address ;
    private WithdrawStatus status ;
    private String remark ;
    private BooleanEnum isAuto ;
    private BooleanEnum isQuick ;

}
