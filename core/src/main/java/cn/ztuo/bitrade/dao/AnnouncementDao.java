package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Announcement;
import org.springframework.data.jpa.repository.Query;

/**
 * @author MrGao
 * @description
 * @date 2018/3/5 15:32
 */
public interface AnnouncementDao extends BaseDao<Announcement> {

    @Query("select max(s.sort) from Announcement s")
    int findMaxSort();
}
