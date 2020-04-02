package cn.ztuo.bitrade.model.update;

import cn.ztuo.bitrade.ability.UpdateAbility;
import cn.ztuo.bitrade.constant.Platform;
import cn.ztuo.bitrade.entity.AppRevision;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:48
 */
@Data
public class AppRevisionUpdate implements UpdateAbility<AppRevision> {

    private String remark;

    private String version;

    private String downloadUrl;

    private Platform platform;

    //转化
    @Override
    public AppRevision transformation(AppRevision appRevision) {
        if (StringUtils.isNotBlank(remark))
            appRevision.setRemark(remark);
        if (StringUtils.isNotBlank(version))
            appRevision.setVersion(version);
        if (StringUtils.isNotBlank(downloadUrl))
            appRevision.setDownloadUrl(downloadUrl);
        if (platform != null)
            appRevision.setPlatform(platform);
        return appRevision;
    }


}
