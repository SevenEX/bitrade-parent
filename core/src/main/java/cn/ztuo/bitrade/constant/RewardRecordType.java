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
public enum RewardRecordType implements BaseEnum {
    /**
     * 推广
     */
    PROMOTION("推广"),
    /**
     * 活动
     */
    ACTIVITY("活动"),
    /**
     * 分红
     */
    DIVIDEND("分红");
    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

}
