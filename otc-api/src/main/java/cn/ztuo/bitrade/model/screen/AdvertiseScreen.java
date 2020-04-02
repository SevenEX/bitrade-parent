package cn.ztuo.bitrade.model.screen;

import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.ability.ScreenAbility;
import cn.ztuo.bitrade.constant.AdvertiseControlStatus;
import cn.ztuo.bitrade.entity.QAdvertise;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2711:45
 */
@Data
public class AdvertiseScreen implements ScreenAbility {

    AdvertiseControlStatus status;

    /**
     * 处理内部断言
     *
     * @return
     */
    @Override
    public ArrayList<BooleanExpression> getBooleanExpressions() {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (status != null)
            booleanExpressions.add(QAdvertise.advertise.status.eq(status));
        return booleanExpressions;
    }


}
