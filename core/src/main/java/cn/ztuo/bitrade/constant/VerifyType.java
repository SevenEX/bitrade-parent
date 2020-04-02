package cn.ztuo.bitrade.constant;

import cn.ztuo.bitrade.core.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 所有只有两种状态的都可使用,ordinal为0表示正常，启用；ordinal为1表示软删除，禁用，取消.<br>
 *
 * @author Seven
 * @date 2019年12月07日
 */
@AllArgsConstructor
@Getter
public enum VerifyType implements BaseEnum {

    ON_EMAIL("开启邮箱验证"),
    OFF_EMAIL("关闭邮箱验证"),
    ON_SMS("开启手机验证"),
    OFF_SMS("关闭手机验证"),
    ON_GOOGLE("开启谷歌验证"),
    OFF_GOOGLE("关闭谷歌验证"),
    RESET_PASSWORD("重置登录密码"),
    CHANGE_PASSWORD("修改登录密码"),
    CHANGE_OTC_PASSWORD("修改法币资金密码");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
