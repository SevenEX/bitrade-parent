package cn.ztuo.bitrade.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.AuditStatus;
import cn.ztuo.bitrade.enums.CredentialsType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import javax.validation.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author MrGao
 * @description 会员审核信息
 * @date 2017/12/26 14:35
 */
@Entity
@Table(name = "member_application")
@Data
public class MemberApplication {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String realName;
    private String idCard;
    //认证类型，0：身份证认证，1：护照认证，2:驾照认证
    @Column(columnDefinition = "int default 0 comment '认证类型'")
    private CredentialsType type;
    /**
     * 证件 正面
     */
    @NotBlank(message = "{MemberApplication.identityCardImgFront.blank}")
    private String identityCardImgFront;
    /**
     * 证件 反面
     */
    @NotBlank(message = "{MemberApplication.identityCardImgReverse.blank}")
    private String identityCardImgReverse;
    /**
     * 证件 手持
     */
    @NotBlank(message = "{MemberApplication.identityCardImgInHand.blank}")
    private String identityCardImgInHand;

    /**
     *  审核状态
     */
    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private AuditStatus auditStatus;

    /**
     * 审核信息所有者
     */
    @JoinColumn(name = "member_id",nullable = false)
    @ManyToOne
    private Member member;

    /**
     * 驳回理由
     */
    private String rejectReason;

    /**
     * 创建时间
     */

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 审核时间
     */
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


    /**
     * kyc2级视频认证URL
     */
    private String videoUrl;

    /**
     * 0-未实名、1-视频审核,2-实名审核失败、3-视频审核失败,4-实名成功,5-待实名审核 ,6-待视频审核
     */
    private Integer kycStatus;


    /**
     * 视频六位随机码
     */
    private String videoRandom;

}
