package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class  MemberScreen extends AccountScreen{

    private String realName;

    /**
     * 身份证号码
     */
    private String idNumber;

    private String email;

    private String mobilePhone;

    /**
     * 会员注册时间
     */

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;

    /**
     * 实名状态：(012 未认证/审核中/已认证)
     */
    private CertifiedBusinessStatus certifiedBusinessStatus;
    /**
     * 01(正常/非法)
     */
    private CommonStatus commonStatus ;

    private BooleanEnum transactionStatus;

    private String promotionCode;


}
