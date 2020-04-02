package cn.ztuo.bitrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * openApi工程
 * @Author MrGao
 */
@EnableScheduling
@SpringBootApplication
@EnableEurekaClient
@EnableSwagger2
public class OpenApiApplication {
    public static void main(String[] args){
        SpringApplication.run(OpenApiApplication.class,args);
    }
}
