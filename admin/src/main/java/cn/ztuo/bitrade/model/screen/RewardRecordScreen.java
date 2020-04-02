package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.RewardRecordType;
import cn.ztuo.bitrade.entity.QRewardRecord;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖励记录查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RewardRecordScreen extends AccountScreen {

    /**
     * 奖励币种单位
     */
    private String unit;
    /**
     * 奖励币种单位
     */
    private String symbol;

    /**
     * 奖励类型
     */
    private RewardRecordType type;


    public Predicate getPredicate() {
        List<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (!StringUtils.isEmpty(symbol)) {
            booleanExpressions.add(QRewardRecord.rewardRecord.coin.unit.eq(symbol));
        }
        if (type != null) {
            booleanExpressions.add(QRewardRecord.rewardRecord.type.eq(type));
        }
        // 模糊查询 UID 手机 邮箱
        if (!StringUtils.isEmpty(keyWords)) {
            booleanExpressions.add(
                    QRewardRecord.rewardRecord.member.mobilePhone.like(keyWords + "%")
//                            .or(QRewardRecord.rewardRecord.member.username.like("%" + keyWords + "%"))
                            .or(QRewardRecord.rewardRecord.member.email.like(keyWords + "%"))
                            .or(QRewardRecord.rewardRecord.member.id.like("%" + keyWords + "%"))
//                    .or(QRewardRecord.rewardRecord.member.realName.like("%" + keyWords + "%"))
            );
        }
        return PredicateUtils.getPredicate(booleanExpressions);
    }
}
