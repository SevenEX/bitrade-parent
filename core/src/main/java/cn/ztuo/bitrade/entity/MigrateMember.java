package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.annotation.Excel;
import cn.ztuo.bitrade.constant.*;
import cn.ztuo.bitrade.vo.ChannelVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 会员用户
 *
 * @author Seven
 * @date 2019年01月02日
 */
@Entity
@Data
public class MigrateMember {

    @Excel(name ="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long foreignId;

    @JsonIgnore
    private String salt;

    @Excel(name = "法币昵称")
    //@NotBlank(message = "用户名不得为空")
    @Column(unique = true)
    private String username;

    /**
     * 登录密码
     */
    @JsonIgnore
    @NotBlank(message = "{Member.password.blank}")
    private String password;

    @Excel(name = "是否缴纳保证金")
    private String margin;

    @Excel(name = "googleKey")
    private String googleKey;

    /**
     * 0-未开启，1-开启
     */
    @Excel(name = "googleState")
    @Column(columnDefinition = "int default 0")
    private Integer googleState = 0;

    /**
     * 0-未开启，1-开启
     */
    @Excel(name = "phoneState")
    @Column(columnDefinition = "int default 0")
    private Integer phoneState = 0;

    /**
     * 0-未开启，1-开启
     */
    @Excel(name = "emailState")
    @Column(columnDefinition = "int default 0")
    private Integer emailState = 0;

    @Excel(name = "googleDate")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date googleDate;
    /**
     * 交易密码
     */
    @JsonIgnore
    private String jyPassword;

    @Excel(name = "会员真实姓名")
    private String realName;

    /**
     * 身份证号码
     */
    @Excel(name = "身份证号码")
    private String idNumber;

    @Excel(name = "邮箱")
    @Column(unique = true)
    private String email;


    @Excel(name = "手机号")
    @Column(unique = true)
    private String mobilePhone;

    @Embedded
    private Location location;

    @Enumerated(EnumType.ORDINAL)
    private MemberLevelEnum memberLevel;

    @Enumerated(EnumType.ORDINAL)
    private CommonStatus status = CommonStatus.NORMAL;

    @Excel(name = "注册时间")
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registrationTime;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    private String token;


    /**
     * 交易次数
     */
    @Excel(name = "交易次数")
    private int transactions = 0;

    @Embedded
    private BankInfo bankInfo;

    @Embedded
    private Alipay alipay;

    @Embedded
    private WechatPay wechatPay;

    /**
     * 申诉次数
     */
    @Excel(name = "申诉次数")
    private int appealTimes = 0;
    /**
     * 胜诉次数
     */
    private int appealSuccessTimes = 0;

    /**
     * 邀请者ID
     */
    @Excel(name = "推广者ID")
    private Long inviterId;

    private Long foreignInviterId;
    /**
     * 推广码
     */
    @Excel(name = "推广码")
    private String promotionCode;
    @Excel(name = "一级推广人数")
    private int firstLevel = 0;
    @Excel(name = "二级推广人数")
    private int secondLevel = 0;
    private int thirdLevel = 0;

    private String local;
    /**
     * 实名认证状态
     */
    @Excel(name = "实名认证状态")
    @Enumerated(EnumType.ORDINAL)
    private RealNameStatus realNameStatus = RealNameStatus.NOT_CERTIFIED;

    /**
     * 登录次数
     */
    private int loginCount = 0;
    /**
     * 认证商家状态
     */
    @Enumerated(EnumType.ORDINAL)
    private CertifiedBusinessStatus certifiedBusinessStatus = CertifiedBusinessStatus.NOT_CERTIFIED;

    /**
     * 认证商家申请时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date certifiedBusinessApplyTime;

    /**
     * 实名认证通过时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applicationTime;


    /**
     * 商家审核通过时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date certifiedBusinessCheckTime;



    /**
     * 头像
     */
    private String avatar;
    /**
     * token预计过期时间
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date tokenExpireTime;

    /**
     * 是否允许发布广告  1表示可以发布
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum publishAdvertise = BooleanEnum.IS_TRUE;

    /**
     * 0表示禁止交易
     */
    @Enumerated(EnumType.ORDINAL)
    private BooleanEnum transactionStatus = BooleanEnum.IS_TRUE;

    /**
     * 签到能力
     */
    @Column(name = "sign_in_ability", columnDefinition = "bit default 1", nullable = false)
    private Boolean signInAbility = true;

    private Date transactionTime; //首次交易时间

    /**
     * 渠道编号，取值UID
     */
    @Column(columnDefinition = "int default 0")
    private Long channelId = 0L;

    /**
     * 是否为渠道
     */
    @Column(columnDefinition = "int default 0")
    private BooleanEnum isChannel = BooleanEnum.IS_FALSE;

    @Transient
    @Embedded
    private ChannelVO channelVO;
    /**
     * 登录锁，3分钟内连续十次输错账号密码时锁定
     */
    private BooleanEnum loginLock=BooleanEnum.IS_FALSE;

    /**
     * 会员积分
     */
    private Long integration =0L;
    /**
     * 会员等级外键 6个等级 V1-V5 根据积分设置 V6手动设置 不收取手续费
     * 默认等级是 V1=1 V2=2 V3=3 V4=4 V5=5 V6=6
     */
    private Long memberGradeId=1L;


    /**
     * 0-未实名、1-视频审核,2-实名审核失败、3-视频审核失败,4-实名成功,5-待实名审核 ,6-待视频审核
     */
    private Integer kycStatus;

    /**
     * 推广会员实名认证积分总和
     */
    private Long generalizeTotal =0L;

    /**
     * 邀请者父级id
     */
    private Long inviterParentId;

    private Long foreignInviterParentId;
    /**
     * 是否使用SE抵扣手续费
     */
    @Column(name = "se_fee_switch", columnDefinition = "bit default 0", nullable = false)
    private Boolean seFeeSwitch = true;

    /**
     * 是否开启快速提币
     */
    @Column(columnDefinition = "int default 0")
    private BooleanEnum isQuick = BooleanEnum.IS_FALSE;
}