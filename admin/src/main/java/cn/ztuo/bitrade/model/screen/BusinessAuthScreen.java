package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.AuditStatus;
import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessAuthScreen extends AccountScreen{
    private CertifiedBusinessStatus status;//审核状态
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date startTime;//发起申请的时间段，起始
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    private Date endTime;//发起申请的时间段，结束
}
