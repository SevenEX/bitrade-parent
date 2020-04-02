package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import cn.ztuo.bitrade.constant.VerifyLevel;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商家认证申请信息
 * @author zhang yingxin
 * @date 2018/5/5
 */
@Entity
@Data
@Table
@ToString
public class BusinessAuthApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    /**
     * 认证商家状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CertifiedBusinessStatus certifiedBusinessStatus;

    /**
     * 认证失败的原因
     */
    private String detail;
    /**
     * 申请时间
     */
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 审核时间
     */
    private Date auditingTime;

    @Column(columnDefinition="TEXT")
    private String authInfo;

    @ManyToOne
    @JoinColumn(name="business_auth_deposit_id")
    private BusinessAuthDeposit businessAuthDeposit;

    private String depositRecordId;

    /**
     * 保证金数额
     */
    @Column(columnDefinition = "decimal(20,8) comment '保证金数额'")
    private BigDecimal amount;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime ;

    @Transient
    private JSONObject info ;

    /**
     * 认证商家加V
     */
    @Enumerated(EnumType.ORDINAL)
    private VerifyLevel verifyLevel;
}
