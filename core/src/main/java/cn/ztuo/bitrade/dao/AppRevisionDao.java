package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.Platform;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.AppRevision;

import java.util.List;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/2416:18
 */
public interface AppRevisionDao extends BaseDao<AppRevision> {
    List<AppRevision> findAppRevisionByPlatformOrderByIdDesc(Platform platform);
}
