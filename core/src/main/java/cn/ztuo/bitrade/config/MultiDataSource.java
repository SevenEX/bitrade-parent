package cn.ztuo.bitrade.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>多数据源配置</p>
 * <p>多个数据源</p>
 *
 * @author maxzhao
 * @date 2019-06-26 16:22
 */
@Component
@ConfigurationProperties(prefix = "gt.maxzhao.boot")
@Getter
@Setter
@Slf4j
public class MultiDataSource {
    public MultiDataSource() {
        log.info("加载多数据源配置信息  -->  {}", "gt.maxzhao.boot.datasource");
    }
    /**
     * 多个数据源
     */
    private List<DruidProperties> datasource;
}
