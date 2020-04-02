package cn.ztuo.bitrade.config;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "druid",name = "monitor",havingValue = "on")
public class DruidMonitorConfig {
    private Logger logger = LoggerFactory.getLogger(DruidMonitorConfig.class);
    /**
     * 注册ServletRegistrationBean
     * @return
     */
    @Bean
    public ServletRegistrationBean registrationBean(@Value("${druid.allow-ips:127.0.0.1}") String allowIps,
                                                    @Value("${druid.deny-ips:}") String denyIps,
                                                    @Value("${druid.username:bitrade}") String username,
                                                    @Value("${druid.password:bitrade123}") String password) {
        logger.info("====注册Druid监控配置===");
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid1/*");
        /** 初始化参数配置，initParams**/
        //白名单
        bean.addInitParameter("allow", allowIps);
        //IP黑名单 (存在共同时，deny优先于allow)
        bean.addInitParameter("deny", denyIps);
        //登录查看信息的账号密码.
        bean.addInitParameter("loginUsername", username);
        bean.addInitParameter("loginPassword", password);
        //是否能够重置数据.
        bean.addInitParameter("resetEnable", "false");
        return bean;
    }

    /**
     * 注册FilterRegistrationBean
     * @return
     */
    @Bean
    public FilterRegistrationBean druidStatFilter() {
        FilterRegistrationBean bean = new FilterRegistrationBean(new WebStatFilter());
        //添加过滤规则.
        bean.addUrlPatterns("/*");
        //添加不需要忽略的格式信息.
        bean.addInitParameter("exclusions","*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid2/*");
        return bean;
    }
}
