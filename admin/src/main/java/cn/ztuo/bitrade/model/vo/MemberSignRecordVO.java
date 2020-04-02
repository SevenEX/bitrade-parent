package cn.ztuo.bitrade.model.vo;

import cn.ztuo.bitrade.entity.Member;
import cn.ztuo.bitrade.entity.MemberSignRecord;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/5/714:22
 */
@Data
@Builder
public class MemberSignRecordVO {

    private Long id;

    private String username;

    private String mobilePhone;

    private String realName;

    private Long signId;

    private String coinName;

    private BigDecimal amount;

    private Date creationTime;

    public static MemberSignRecordVO getMemberSignRecordVO(MemberSignRecord x) {
        Member member = x.getMember();
        return MemberSignRecordVO.builder()
                .id(x.getId())
                .username(member.getUsername())
                .mobilePhone(member.getMobilePhone())
                .realName(member.getRealName())
                .signId(x.getSign().getId())
                .coinName(x.getCoin().getName())
                .amount(x.getAmount())
                .creationTime(x.getCreationTime())
                .build();
    }

}
