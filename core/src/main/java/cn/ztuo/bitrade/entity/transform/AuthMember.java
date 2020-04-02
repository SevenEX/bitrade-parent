package cn.ztuo.bitrade.entity.transform;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.MemberLevelEnum;
import cn.ztuo.bitrade.entity.Location;
import cn.ztuo.bitrade.entity.Member;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Seven
 * @date 2019年01月11日
 */
@Builder
@Data
public class AuthMember implements Serializable {
    private static final long serialVersionUID = -4199550203850153635L;
    private long id;
    private String name;
    private String realName;
    private Location location;
    private String mobilePhone;
    private String email;
    private MemberLevelEnum memberLevel;
    private CommonStatus status;
    private Long memberGradeId;

    /**
     * 如需添加信息在{@link #toAuthMember(Member)}方法中添加
     *
     * @param member
     * @return
     */
    public static AuthMember toAuthMember(Member member) {
        return AuthMember.builder()
                .id(member.getId())
                .name(StringUtils.isNotEmpty(member.getUsername())?member.getUsername():"")
                .realName(member.getRealName())
                .location(member.getLocation())
                .mobilePhone(member.getMobilePhone())
                .email(member.getEmail())
                .memberLevel(member.getMemberLevel())
                .status(member.getStatus())
                .memberGradeId(member.getMemberGradeId())
                .build();
    }

}
