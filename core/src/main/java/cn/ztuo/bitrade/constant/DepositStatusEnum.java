package cn.ztuo.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 保证金状态
 * @author zhang yingxin
 * @date 2018/5/5
 */
@AllArgsConstructor
@Getter
public enum DepositStatusEnum  implements BaseEnum {
    PAY("缴纳"),
    GET_BACK("索回");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }
}
