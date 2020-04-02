package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.SysRole;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Seven
 * @date 2019年12月18日
 */
public interface SysRoleDao extends BaseDao<SysRole> {

    @Modifying
    @Query("update SysRole s set s.description=?1,s.role=?2 where s.id=?3")
    int updateSysRole(String description, String role, Long id);

    @Query("SELECT new SysRole(s.id,s.role,s.description) FROM SysRole s")
    List<SysRole> findAllSysRole();

}
