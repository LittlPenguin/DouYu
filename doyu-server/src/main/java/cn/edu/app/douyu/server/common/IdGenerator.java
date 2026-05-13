package cn.edu.app.douyu.server.common;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {
    public String next(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
