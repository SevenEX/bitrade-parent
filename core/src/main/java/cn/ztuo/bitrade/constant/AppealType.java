package cn.ztuo.bitrade.constant;

import cn.ztuo.bitrade.core.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Seven
 * @date 2019年01月22日
 */
@AllArgsConstructor
@Getter
public enum AppealType implements BaseEnum {
    NO_COIN("我已付款成功，卖家未及时放行"),
    NO_PAID("我并没有收到买家的转账"),
    MORE_PAID("我向卖家多转了钱"),
    MORE_COIN("收到买家付款，但是金额不符"),
    OTHER("其他");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return this.ordinal();
    }
}
