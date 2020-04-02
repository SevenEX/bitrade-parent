package cn.ztuo.bitrade.entity.transform;

import cn.ztuo.bitrade.constant.AdvertiseControlStatus;
import cn.ztuo.bitrade.constant.AdvertiseType;
import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.PriceType;
import cn.ztuo.bitrade.entity.Advertise;
import cn.ztuo.bitrade.entity.Country;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @author Seven
 * @date 2019年01月09日
 */
@Builder
@Data
public class MemberAdvertiseDetail implements Serializable{

    private Long id;

    private Long coinId;

    private String coinName;

    private String coinNameCn;

    private String coinUnit;

    private Country country;

    private PriceType priceType;


    /**
     * 交易价格(及时变动)
     */
    private BigDecimal price;

    /**
     * 广告类型 0:买入 1:卖出
     */
    private AdvertiseType advertiseType;


    /**
     * 最低单笔交易额
     */
    private BigDecimal minLimit;

    /**
     * 最高单笔交易额
     */
    private BigDecimal maxLimit;

    /**
     * 备注
     */
    private String remark;

    /**
     * 付款期限，单位分钟
     */
    private Integer timeLimit;

    /**
     * 溢价百分比
     */
    private BigDecimal premiseRate;


    /**
     * 付费方式(用英文逗号隔开)
     */
    private String payMode;


    /**
     * 广告状态
     */
    private AdvertiseControlStatus status ;

    private BigDecimal number;

    /**
     * 市场价
     */
    private BigDecimal marketPrice;

    private BooleanEnum auto;

    private String autoword;

    public static MemberAdvertiseDetail toMemberAdvertiseDetail(Advertise advertise){
        return MemberAdvertiseDetail.builder()
                .id(advertise.getId())
                .advertiseType(advertise.getAdvertiseType())
                .coinId(advertise.getCoin().getId())
                .coinName(advertise.getCoin().getName())
                .coinNameCn(advertise.getCoin().getNameCn())
                .coinUnit(advertise.getCoin().getUnit())
                .country(advertise.getCountry())
                .auto(advertise.getAuto())
                .maxLimit(advertise.getMaxLimit())
                .minLimit(advertise.getMinLimit())
                .number(advertise.getNumber())
                .payMode(advertise.getPayMode())
                .premiseRate(advertise.getPremiseRate())
                .price(advertise.getPrice())
                .priceType(advertise.getPriceType())
                .remark(advertise.getRemark())
                .status(advertise.getStatus())
                .timeLimit(advertise.getTimeLimit())
                .autoword(advertise.getAutoword())
                .build();
    }


}
