package cn.ztuo.bitrade.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.ztuo.bitrade.constant.SignStatus;
import cn.ztuo.bitrade.entity.Sign;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/5/715:12
 */
@Data
@Builder
public class SignVO {
    private Long id;

    /**
     * 币种名称
     */
    private String name;

    /**
     * 币种单位
     */
    private String unit;

    /**
     * 赠送数目
     */
    private BigDecimal amount;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creationTime;

    /**
     * 签到活动状态
     */
    private SignStatus status;

    public static SignVO getSignVO(Sign x) {
        return SignVO.builder()
                .id(x.getId())
                .name(x.getCoin().getName())
                .unit(x.getCoin().getUnit())
                .amount(x.getAmount())
                .endDate(x.getEndDate())
                .creationTime(x.getCreationTime())
                .status(x.getStatus())
                .build();
    }
}
