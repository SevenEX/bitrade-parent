package cn.ztuo.bitrade.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum WalletEnum {
    NORMAL("正常"),
    EXPLOSION("爆仓"),
    CLOSE_POSITION("平仓");

    @Setter
    private String cnName;

    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

}
