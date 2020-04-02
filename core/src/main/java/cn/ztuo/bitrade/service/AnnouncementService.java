package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.dao.AnnouncementDao;
import cn.ztuo.bitrade.entity.Announcement;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author MrGao
 * @description
 * @date 2018/3/5 15:24
 */
@Service
public class AnnouncementService extends BaseService<Announcement> {
    @Autowired
    private AnnouncementDao announcementDao;

    public Announcement save(Announcement announcement) {
        return announcementDao.save(announcement);
    }

    public List<Announcement> findAll() {
        return announcementDao.findAll();
    }

    public Announcement findById(Long id) {
        return announcementDao.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        announcementDao.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            Announcement announcement = findById(id);
            Assert.notNull(announcement, "validate id!");
            deleteById(id);
        }
    }

    public int getMaxSort(){
        return announcementDao.findMaxSort();
    }

    public Page<Announcement> findAll(Predicate predicate, Pageable pageable) {
        return announcementDao.findAll(predicate, pageable);
    }
}
