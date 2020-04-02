package cn.ztuo.bitrade.config;

import cn.ztuo.bitrade.db.DynamicDataSource;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>多数据源配置</p>
 * <p>多数据源配置</p>
 *
 * @author maxzhao
 * @date 2019-06-26 16:07
 */
@Slf4j
@Component
public class MultiSourceConfig {
    @Autowired
    private DruidProperties druidProperties;

    @Autowired
    private MultiDataSource multiDataSource;


    /**
     * 单数据源连接池配置
     */
    @Bean("dataSource")
    @ConditionalOnProperty(name = "gt.maxzhao.boot.multiDatasourceOpen", havingValue = "false")
    public DruidDataSource singleDatasource() {
        log.info("singleDatasource");
        return druidProperties.config(new DruidDataSource());
    }

    /**
     * 多数据源连接池配置
     */
    @Bean("dataSource")
    @ConditionalOnProperty(name = "gt.maxzhao.boot.multiDatasourceOpen", havingValue = "true")
    public DynamicDataSource mutiDataSource() {
        log.info("mutiDataSource");

        //存储数据源别名与数据源的映射
        HashMap<Object, Object> dbNameMap = new HashMap<>();
        // 核心数据源
        DruidDataSource mainDataSource = druidProperties.config();
        // 这里添加 主要数据库，其它数据库挂了，默认使用主数据库
        dbNameMap.put("main", mainDataSource);
        // 其它数据源
        // 当前多数据源是否存在
        if (multiDataSource.getDatasource() != null) {
            //过滤掉没有添加 dbName 的数据源，先加载娟全局配置，再次加载当前配置
            List<DruidDataSource> multiDataSourceList = multiDataSource.getDatasource().stream()
                    .filter(dp -> !"".equals(Optional.ofNullable(dp.getDbName()).orElse("")))
                    .map(dp -> {
                        DruidDataSource druidDataSource = dp.config(druidProperties.config());
                        dbNameMap.put(dp.getDbName(), druidDataSource);
                        return druidDataSource;
                    })
                    .collect(Collectors.toList());

            // 测试所有的数据源
            try {
                mainDataSource.init();
                for (DruidDataSource druidDataSource : multiDataSourceList) {
                    druidDataSource.init();
                }
            } catch (SQLException sql) {
                log.error("=======================    多数据源配置错误   ==========================");
                sql.printStackTrace();
            }
        }
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(dbNameMap);
        dynamicDataSource.setDefaultTargetDataSource(mainDataSource);
        return dynamicDataSource;
    }

}