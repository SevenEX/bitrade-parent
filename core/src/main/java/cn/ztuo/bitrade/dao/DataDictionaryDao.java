package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.DataDictionary;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:15
 */
public interface DataDictionaryDao extends BaseDao<DataDictionary> {
    DataDictionary findByBond(String bond);

    List<DataDictionary> findByComment(String comment);

    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("update DataDictionary dateDictionary set dateDictionary.value =  :value where dateDictionary.bond = :bond")
    void updateByBond(@Param("bond") String bond, @Param("value") String value);

}
