package cn.ztuo.bitrade.model.screen;

import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.ability.ScreenAbility;
import cn.ztuo.bitrade.entity.QSign;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * @author MrGao
 * @Description:
 * @date 2018/5/315:53
 */
@Data
public class SignScreen implements ScreenAbility {

    private String unit;

    @Override
    public ArrayList<BooleanExpression> getBooleanExpressions() {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(unit))
            booleanExpressions.add(QSign.sign.coin.unit.eq(unit));
        return booleanExpressions;
    }
}
