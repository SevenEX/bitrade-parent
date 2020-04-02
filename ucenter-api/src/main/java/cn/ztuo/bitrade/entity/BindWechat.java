package cn.ztuo.bitrade.entity;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * @author Seven
 * @date 2019年01月16日
 */
@Data
public class BindWechat {
    @NotBlank(message = "{BindWechat.realName.null}")
    private String realName;
    @NotBlank(message = "{BindWechat.wechat.null}")
    private String wechat;
    @NotBlank(message = "{BindWechat.jyPassword.null}")
    private String jyPassword;
    /** 微信收款二维码 */
    private String qrCodeUrl;
}
