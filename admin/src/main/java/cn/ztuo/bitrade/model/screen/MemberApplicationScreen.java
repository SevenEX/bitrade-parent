package cn.ztuo.bitrade.model.screen;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.AuditStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberApplicationScreen extends AccountScreen{
    private AuditStatus auditStatus;//审核状态
    private String idNumber ; //身份证号
    private String realName;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date approveStartTime;//发起申请的时间段，起始
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date approveEndTime;//发起申请的时间段，结束
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date auditStartTime;//审核时间段，起始
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date auditEndTime;//审核时间段，结束

    /**
     * 0-未实名、1-视频审核,2-实名审核失败、3-视频审核失败,4-实名成功,5-待实名审核 ,6-待视频审核
     */
    private Integer kycStatus;
}
