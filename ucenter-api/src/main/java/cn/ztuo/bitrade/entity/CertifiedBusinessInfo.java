package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import cn.ztuo.bitrade.constant.MemberLevelEnum;
import lombok.Data;

/**
 * @author Seven
 * @date 2019年02月26日
 */
@Data
public class CertifiedBusinessInfo {
    private MemberLevelEnum memberLevel;
    private CertifiedBusinessStatus certifiedBusinessStatus;
    private String email;
    /**
     * * 审核失败原因
     */
    private String detail;
    /**
     *
     * 退保原因
     */
    private String reason ;

    private  BusinessCancelApply businessCancelApply;

    private BusinessAuthApply businessAuthApply;
}
