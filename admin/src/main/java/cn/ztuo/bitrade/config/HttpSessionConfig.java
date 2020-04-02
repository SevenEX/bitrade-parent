package cn.ztuo.bitrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * 一个小时过期
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds =3600, redisNamespace = "spring:admin:session")
public class HttpSessionConfig {
	@Bean
	public HttpSessionIdResolver httpSessionIdResolver() {
		return  new CookieHttpSessionIdResolver();
	}
}
