package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Navigation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NavigationDao extends BaseDao<Navigation> {

    @Query(value = "SELECT * FROM navigation s WHERE s.status ='0' and s.locale = :locale and s.type = :type ORDER BY s.sort DESC", nativeQuery = true)
    List<Navigation> findAllByStatusNotAndTypeAndLocale(@Param("type") String type,@Param("locale") String locale);
}
