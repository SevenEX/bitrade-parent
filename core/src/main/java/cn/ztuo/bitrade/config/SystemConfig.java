package cn.ztuo.bitrade.config;

import cn.ztuo.bitrade.core.DB;
import cn.ztuo.bitrade.util.IdWorkByTwitter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Seven
 * @date 2019年12月22日
 */
@Configuration
public class SystemConfig {

    @Bean
    public IdWorkByTwitter idWorkByTwitter(@Value("${bdtop.system.work-id:0}")long workId,@Value("${bdtop.system.data-center-id:0}")long dataCenterId){
        return new IdWorkByTwitter(workId, dataCenterId);
    }

    @Bean
    public DB db(@Qualifier("dataSource") DataSource dataSource){
        return new DB(dataSource, true);
    }

}
