package cn.edu.app.douyu.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 后端启动入口：加载 Spring Boot、异步能力和配置属性扫描。
 */
@EnableAsync
@ConfigurationPropertiesScan
@SpringBootApplication
public class DouyuServerApplication {

    // 启动 /api/v1 服务，Controller、Security、Repository 等 Bean 由 Spring 自动装配。
    public static void main(String[] args) {
        SpringApplication.run(DouyuServerApplication.class, args);
    }
}
