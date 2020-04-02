package cn.ztuo.bitrade.entity;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * @author Seven
 * @date 2019年01月16日
 */
@Data
public class BindAli {
    @NotBlank(message = "{BindAli.realName.null}")
    private String realName;
    @NotBlank(message = "{BindAli.ali.null}")
    private String ali;
    @NotBlank(message = "{BindAli.jyPassword.null}")
    private String jyPassword;
    /** 支付宝收款二维码 */
    private String qrCodeUrl;
}
