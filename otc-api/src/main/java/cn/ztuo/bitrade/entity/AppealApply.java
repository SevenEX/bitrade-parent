package cn.ztuo.bitrade.entity;

import cn.ztuo.bitrade.constant.AppealType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author Seven
 * @date 2019年01月22日
 */
@Data
public class AppealApply {
    @NotNull(message = "{AppealApply.orderSn.null}")
    @ApiModelProperty(value="申诉订单号")
    private String orderSn;
    @NotBlank(message = "AppealApply.remark.null")
    @ApiModelProperty(value="申诉理由")
    private String remark;

    private String imgUrls;

    @ApiModelProperty(value="申诉类型")
    private AppealType type;
}
