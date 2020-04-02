package cn.ztuo.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Paradise
 */
@ApiModel(value = "数据统计-法币交易")
@Data
@AllArgsConstructor
public class StatisticsOtcDto {

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    /**
     * 交易对
     */
    @ApiModelProperty(value = "币种")
    private String unit;

    /**
     * 交易量
     */
    @ApiModelProperty(value = "交易量")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易额")
    private BigDecimal money;

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee;

}
