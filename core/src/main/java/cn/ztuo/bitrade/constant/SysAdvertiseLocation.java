package cn.ztuo.bitrade.constant;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author MrGao
 * @description 系统广告位置
 * @date 2018/1/6 15:59
 */
@AllArgsConstructor
@Getter
public enum SysAdvertiseLocation implements BaseEnum {
    //app 首页轮播
    APP_SHUFFLING("app首页轮播"),
    //pc 首页轮播
    PC_SHUFFLING("pc首页轮播"),
    //app 分类广告
    APP_CLASSIFICATION("app首页广告(中部)");
    @Setter
    private String cnName;
    @Override
    @JsonValue
    public int getOrdinal() {
        return ordinal();
    }

}
