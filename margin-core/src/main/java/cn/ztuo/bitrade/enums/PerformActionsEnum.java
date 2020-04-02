package cn.ztuo.bitrade.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum PerformActionsEnum {
    Warning("警报"),
    Explosion("爆仓");

    @Setter
    private String cnName;

    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
