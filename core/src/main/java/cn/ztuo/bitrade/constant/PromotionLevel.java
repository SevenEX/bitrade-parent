package cn.ztuo.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Seven
 * @date 2019年03月08日
 */
@AllArgsConstructor
@Getter
public enum PromotionLevel implements BaseEnum {
    /**
     * 一级
     */
    ONE("一级"),
    /**
     * 二级
     */
    TWO("二级"),
    /**
     * 三级
     */
    THREE("三级");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }
}
