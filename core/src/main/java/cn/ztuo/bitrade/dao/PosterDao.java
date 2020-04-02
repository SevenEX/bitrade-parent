package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.Poster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Paradise
 */
public interface PosterDao extends BaseDao<Poster> {

    /**
     * 根据语种查询海报列表
     *
     * @param locale 语种
     * @return select res
     */
    @Query(value = "SELECT * FROM poster s WHERE s.status ='0' and s.locale = :locale ORDER BY s.create_time DESC", nativeQuery = true)
    List<Poster> findAllByLocale(@Param("locale") String locale);
}
