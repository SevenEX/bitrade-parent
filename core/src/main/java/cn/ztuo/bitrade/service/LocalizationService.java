package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.annotation.RedisCache;
import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.dao.SysLocalizationDao;
import cn.ztuo.bitrade.entity.SysLocalization;
import cn.ztuo.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocalizationService extends BaseService<SysLocalization> {
    @Autowired
    SysLocalizationDao sysLocalizationDao;

    @RedisCache(RedissonKeyConstant.CACHE_LOCALE)
    public LinkedHashMap<String, LinkedHashMap<String, String>> getAllMessage(){
        return getAllMessageWithoutCache();
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getAllMessageWithoutCache(){
        List<SysLocalization> all = sysLocalizationDao.findAll(Sort.by(Sort.Direction.ASC, "locale", "id"));
        return all.stream().collect(
                Collectors.groupingBy(SysLocalization::getLocale, LinkedHashMap::new,
                        Collectors.groupingBy(SysLocalization::getId, LinkedHashMap::new,
                                Collectors.mapping(SysLocalization::getContent, Collectors.joining(";")))));
    }

    public int updateMessage(SysLocalization sysLocalization){
        return sysLocalizationDao.updateSysLocalization(sysLocalization.getContent(), sysLocalization.getId(), sysLocalization.getLocale());
    }
}
