package cn.ztuo.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 广告类型
 *
 * @author Seven
 * @date 2019年12月07日
 */
@AllArgsConstructor
@Getter
public enum AdvertiseType implements BaseEnum {

    /**
     * 购买
     */
    BUY("购买"),

    /**
     * 出售
     */
    SELL("出售");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }

}
