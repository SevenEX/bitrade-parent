package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.annotation.RedisCache;
import cn.ztuo.bitrade.constant.Locale;
import cn.ztuo.bitrade.constant.RedissonKeyConstant;
import cn.ztuo.bitrade.dao.LocalizationExtendDao;
import cn.ztuo.bitrade.entity.LocalizationExtend;
import cn.ztuo.bitrade.entity.QLocalizationExtend;
import cn.ztuo.bitrade.service.Base.BaseService;
import com.google.api.client.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class LocalizationExtendService extends BaseService {
    @Autowired
    private LocalizationExtendDao localizationExtendDao;

    @RedisCache(value = RedissonKeyConstant.CACHE_LOCALE_EXTEND, ignoreParam = false)
    public String getLocaleInfo(String tableName, String locale, String busiKey, String columnName) {
        if(!Locale.locales.contains(locale)){
            locale = Locale.ZH_CN;
        }
        LocalizationExtend localizationExtendEntity = localizationExtendDao.findOne(
                QLocalizationExtend.localizationExtend.tableName.eq(tableName)
                        .and(QLocalizationExtend.localizationExtend.busiKey.eq(busiKey))
                        .and(QLocalizationExtend.localizationExtend.locale.eq(locale))
                        .and(QLocalizationExtend.localizationExtend.columnName.eq(columnName))).orElse(null);
        return localizationExtendEntity == null ? null : localizationExtendEntity.getContent();
    }

    public void updateLocaleInfo(String tableName, String locale, String busiKey, String columnName, String content) {
        Assert.state(Locale.locales.contains(locale), "illegal locale");
        LocalizationExtend localizationExtendEntity = localizationExtendDao.findOne(
                QLocalizationExtend.localizationExtend.tableName.eq(tableName)
                .and(QLocalizationExtend.localizationExtend.busiKey.eq(busiKey))
                .and(QLocalizationExtend.localizationExtend.locale.eq(locale))
                .and(QLocalizationExtend.localizationExtend.columnName.eq(columnName))).orElseGet(() -> {
            LocalizationExtend localizationExtend = new LocalizationExtend();
            localizationExtend.setTableName(tableName);
            localizationExtend.setBusiKey(busiKey);
            localizationExtend.setColumnName(columnName);
            localizationExtend.setLocale(locale);
            return localizationExtend;
        });
        localizationExtendEntity.setContent(content);
        localizationExtendDao.save(localizationExtendEntity);
    }

    public List<LocalizationExtend> getLocaleInfo(String tableName, String busiKey) {
        Iterable<LocalizationExtend> localizationExtendEntity = localizationExtendDao.findAll(
                QLocalizationExtend.localizationExtend.tableName.eq(tableName)
                        .and(QLocalizationExtend.localizationExtend.busiKey.eq(busiKey)));
        return localizationExtendEntity == null ? null : Lists.newArrayList(localizationExtendEntity);
    }
}
