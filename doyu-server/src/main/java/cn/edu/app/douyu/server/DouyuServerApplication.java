package cn.edu.app.douyu.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@ConfigurationPropertiesScan
@SpringBootApplication
public class DouyuServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DouyuServerApplication.class, args);
    }
}
