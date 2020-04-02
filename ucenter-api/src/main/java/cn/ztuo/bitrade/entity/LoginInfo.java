package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.MemberLevelEnum;
import cn.ztuo.bitrade.util.MaskUtil;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author Seven
 * @date 2019年01月31日
 */
@Data
@Builder
public class LoginInfo {
    private String username;
    private Location location;
    private MemberLevelEnum memberLevel;
    private String token;
    private String realName;
    private Country country;
    private String avatar;
    private String promotionCode;
    private long id;
    private Integer googleState;
    private Integer kycStatus;
    private Date tokenExpireTime;
    private Boolean seFeeSwitch;
    private String tokenWeb;
    private Date tokenWebExpireTime;

    /**
     * 推广地址前缀
     */
    private String promotionPrefix;

    /**
     * 签到能力
     */
    private Boolean signInAbility;

    /**
     * 是否存在签到活动
     */
    private Boolean signInActivity;
    /**
     * 等级Id
     */
    private Long memberGradeId ;
    /**
     * 用户当前积分
     */
    private Long integration ;

    /**
     * 当前用户手机号
     */
    private String mobile;

    private Date lastLoginTime;

    private String emailRemind;

    private String smsRemind;

    /**
     *
     * @param member
     * @param token
     * @param signInActivity
     * @param prefix
     * @return
     */

    public static LoginInfo getLoginInfo(Member member, String token, Boolean signInActivity, String prefix) {
        return LoginInfo.builder().location(member.getLocation())
                .memberLevel(member.getMemberLevel())
                .username(member.getUsername())
                .token(token)
                .tokenExpireTime(member.getTokenExpireTime())
                .realName(member.getRealName())
                .country(member.getCountry())
                .avatar(member.getAvatar())
                .promotionCode(member.getPromotionCode())
                .id(member.getId())
                .promotionPrefix(prefix)
                .signInAbility(member.getSignInAbility())
                .signInActivity(signInActivity)
                .memberGradeId(member.getMemberGradeId())
                .googleState(member.getGoogleState())
                .integration(member.getIntegration())
                .kycStatus(member.getKycStatus())
                .mobile(MaskUtil.maskMobile(member.getMobilePhone()))
                .lastLoginTime(member.getLastLoginTime())
                .smsRemind(member.getSmsRemind())
                .emailRemind(member.getEmailRemind())
                .seFeeSwitch(member.getSeFeeSwitch())
                .build();

    }
}
