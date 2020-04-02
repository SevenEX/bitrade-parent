package cn.ztuo.bitrade.model.screen;

import com.querydsl.core.types.dsl.BooleanExpression;
import cn.ztuo.bitrade.ability.ScreenAbility;
import cn.ztuo.bitrade.constant.Platform;
import cn.ztuo.bitrade.entity.QAppRevision;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2417:20
 */
@Data
public class AppRevisionScreen implements ScreenAbility {

    private String version;

    private Platform platform;

    @Override
    public ArrayList<BooleanExpression> getBooleanExpressions() {
        ArrayList<BooleanExpression> booleanExpressions = new ArrayList<>();
        if (StringUtils.isNotBlank(version))
            booleanExpressions.add(QAppRevision.appRevision.version.eq(version));
        if (platform != null)
            booleanExpressions.add(QAppRevision.appRevision.platform.eq(platform));
        return booleanExpressions;
    }

}
