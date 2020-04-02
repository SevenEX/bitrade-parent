package cn.ztuo.bitrade.service;

import com.querydsl.core.types.Predicate;
import cn.ztuo.bitrade.dao.SysPermissionDao;
import cn.ztuo.bitrade.entity.SysPermission;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Seven
 * @date 2019年12月20日
 */
@Service
public class SysPermissionService extends BaseService<SysPermission> {

    @Autowired
    public void setDao(SysPermissionDao dao) {
        super.setDao(dao);
    }

    @Autowired
    private SysPermissionDao sysPermissionDao;

    public SysPermission findOne(Long id) {
        return sysPermissionDao.findById(id).orElse(null);
    }

    public List<SysPermission> findAll() {
        return sysPermissionDao.findAll();
    }

    public SysPermission save(SysPermission sysPermission) {
        return sysPermissionDao.save(sysPermission);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletes(Long[] ids) {
        for (long id : ids) {
            sysPermissionDao.deletePermission(id);
            sysPermissionDao.deleteById(id);
        }
    }

    public Page<SysPermission> findAll(Predicate predicate, Pageable pageable) {
        return sysPermissionDao.findAll(predicate, pageable);
    }

    public SysPermission findByPermissionName(String name) {
        return sysPermissionDao.findSysPermissionByName(name);
    }
}
