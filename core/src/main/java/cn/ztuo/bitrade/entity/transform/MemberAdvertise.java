package cn.ztuo.bitrade.entity.transform;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.AdvertiseControlStatus;
import cn.ztuo.bitrade.constant.AdvertiseType;
import cn.ztuo.bitrade.entity.Advertise;
import cn.ztuo.bitrade.entity.Country;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Seven
 * @date 2019年01月10日
 */
@Data
@Builder
public class MemberAdvertise {
    private long id;
    private AdvertiseType advertiseType;
    /**
     * 最低单笔交易额
     */
    private BigDecimal minLimit;

    /**
     * 最高单笔交易额
     */
    private BigDecimal maxLimit;
    private AdvertiseControlStatus status;
    private BigDecimal remainAmount;

    private String coinUnit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Country country;

    public static MemberAdvertise toMemberAdvertise(Advertise x) {
        return MemberAdvertise.builder().id(x.getId())
                .advertiseType(x.getAdvertiseType())
                .coinUnit(x.getCoin().getUnit())
                .createTime(x.getCreateTime())
                .minLimit(x.getMinLimit())
                .maxLimit(x.getMaxLimit())
                .remainAmount(x.getRemainAmount())
                .status(x.getStatus())
                .country(x.getCountry())
                .build();
    }

}
