package cn.ztuo.bitrade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/289:45
 */
@Data
@AllArgsConstructor
public class CoinInfo {
    @NotBlank(message = "{CoinInfo.name.blank}")
    private String name;
    @NotBlank(message = "{CoinInfo.cnName.blank}")
    private String cnName;
    @NotBlank(message = "{CoinInfo.locale.blank}")
    private String locale;
    /**
     * 币种简介
     */
    private String description;
    /**
     * 提币规则
     */
    private String withdrawMessage;
    /**
     * 充币规则
     */
    private String depositMessage;

}
