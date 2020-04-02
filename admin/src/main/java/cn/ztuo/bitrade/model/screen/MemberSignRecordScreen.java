package cn.ztuo.bitrade.model.screen;

import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.ability.ScreenAbility;
import cn.ztuo.bitrade.entity.QMemberSignRecord;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * @author MrGao
 * @Description:
 * @date 2018/5/410:30
 */
@Data
public class MemberSignRecordScreen implements ScreenAbility {
    private String coinName;
    private String username;
    private Long signId;

    @Override
    public ArrayList<BooleanExpression> getBooleanExpressions() {
        if (StringUtils.isNotBlank(username))
            booleanExpressions.add(QMemberSignRecord.memberSignRecord.member.username.eq(username));
        if (signId != null)
            booleanExpressions.add(QMemberSignRecord.memberSignRecord.sign.id.eq(signId));
        if (StringUtils.isNotBlank(coinName))
            booleanExpressions.add(QMemberSignRecord.memberSignRecord.coin.name.eq(coinName));
        return booleanExpressions;
    }
}
