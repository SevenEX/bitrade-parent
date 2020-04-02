package cn.ztuo.bitrade.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Paradise
 */
@ApiModel(value = "数据统计-币币交易")
@Data
public class StatisticsExchangeDto {

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    /**
     * 交易对
     */
    @ApiModelProperty(value = "结算币种")
    private String baseSymbol;
    /**
     * 交易对
     */
    @ApiModelProperty(value = "交易币种")
    private String coinSymbol;

    /**
     * 交易量
     */
    @ApiModelProperty(value = "交易量")
    private BigDecimal amount;

    @ApiModelProperty(value = "交易额")
    private BigDecimal money;
}
