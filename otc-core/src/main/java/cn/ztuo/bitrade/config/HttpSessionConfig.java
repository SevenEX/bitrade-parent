package cn.ztuo.bitrade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;


@EnableRedisHttpSession
public class HttpSessionConfig {
	@Bean
	public HttpSessionIdResolver httpSessionIdResolver() {
		return  new HeaderHttpSessionIdResolver("x-auth-token");
	}
}
