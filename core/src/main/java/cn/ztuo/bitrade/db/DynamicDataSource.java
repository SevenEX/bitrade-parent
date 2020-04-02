package cn.ztuo.bitrade.db;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * <p>多数据源配置</p>
 * <p>动态数据源</p>
 * <p>多 datasource 的上下文</p>
 *
 * @author xiongneng
 * @since 2019年3月5日 上午9:11:49
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    /**
     * <p>多 datasource 的上下文</p>
     * <p>每个线程独立的数据库连接名称</p>
     */
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    /**
     * @param dataSourceDbName 数据库别名
     * @Description: 设置数据源别名
     */
    public static void setDataSourceDbName(String dataSourceDbName) {
        contextHolder.set(dataSourceDbName);
    }

    /**
     * @Description: 获取数据源别名
     */
    public static String getDataSourceDbName() {
        return contextHolder.get();
    }

    /**
     * @Description: 清除数据源别名
     */
    public static void clearDataSourceDbName() {
        contextHolder.remove();
    }

    /**
     * 重写获取连接名称的方法
     * @return 连接名称
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return getDataSourceDbName();
    }

}