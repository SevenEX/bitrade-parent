package cn.ztuo.bitrade.dto;

import cn.ztuo.bitrade.constant.CertifiedBusinessStatus;
import cn.ztuo.bitrade.constant.VerifyLevel;
import cn.ztuo.bitrade.entity.BusinessAuthDeposit;
import cn.ztuo.bitrade.entity.Member;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Data
@AllArgsConstructor
@Builder
public class BusinessDTO {
    private Long id;

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

    private long successCount30 = 0;
    private BigDecimal successRate30 = BigDecimal.ZERO;

    public BusinessDTO() {

    }
}
