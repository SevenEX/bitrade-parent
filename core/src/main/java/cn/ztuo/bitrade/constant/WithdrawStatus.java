package cn.ztuo.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Seven
 * @date 2019年02月25日
 */
@AllArgsConstructor
@Getter
public enum WithdrawStatus implements BaseEnum {
    PROCESSING("等待审核"),
    WAITING("等待放币"),
    FAIL("转账失败"),
    SUCCESS("转账成功"),
    TRANSFER("正在转账"),//异步打钱才有的状态
    WAITINGTRANSFER("等待转账");//请求钱包转账钱状态
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
