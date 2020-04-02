package cn.ztuo.bitrade.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum PaymentType {
    TURN_INTO("转入"),
    TURN_OUT("转出"),
    LOAN("借贷"),
    REPAYMENT("还款"),
    LEVER_EXCHAGE("杠杆币币交易");

    @Setter
    private String cnName;

    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
