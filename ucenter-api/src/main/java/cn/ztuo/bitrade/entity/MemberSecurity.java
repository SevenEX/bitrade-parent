package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.MemberLevelEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author Seven
 * @date 2019年01月15日
 */
@Builder
@Data
public class MemberSecurity {
    private String username;
    private long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private BooleanEnum realVerified;
    private BooleanEnum emailVerified;
    private BooleanEnum phoneVerified;
    private BooleanEnum loginVerified;
    private BooleanEnum fundsVerified;
    private BooleanEnum googleVerified;
    private BooleanEnum realAuditing;
    private String mobilePhone;
    private String email;
    private String realName;
    private String realNameRejectReason;
    private String idCard;
    private String avatar;
    private BooleanEnum accountVerified;
    private Integer googleStatus;
    private int transactions;
    private Date transactionTime; //首次交易时间
    private Integer level;
    private Long integration;
    /**
     * 0-未实名、1-视频审核,2-实名审核失败、3-视频审核失败,4-实名成功,5-待实名审核 ,6-待视频审核
     */
    private Integer kycStatus;
    private Long memberGradeId;
    //private Integer googleState;
    private String securityMessage;
    private MemberLevelEnum memberLevel;

    private BooleanEnum isQuick;

}
