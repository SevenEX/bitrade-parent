package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.DataDictionaryDao;
import cn.ztuo.bitrade.entity.DataDictionary;
import cn.ztuo.bitrade.service.Base.TopBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:19
 */
@Service
public class DataDictionaryService extends TopBaseService<DataDictionary, DataDictionaryDao> {
    @Autowired
    DataDictionaryDao dataDictionaryDao;

    @Autowired
    public void setDao(DataDictionaryDao dao) {
        super.setDao(dao);
    }

    public DataDictionary findByBond(String bond) {
        return dataDictionaryDao.findByBond(bond);
    }

    public List<DataDictionary> findByComment(String comment) {
        return dataDictionaryDao.findByComment(comment);
    }

    public void updateByBond(String bond, String value) {
        dataDictionaryDao.updateByBond(bond,value);
    }

}
