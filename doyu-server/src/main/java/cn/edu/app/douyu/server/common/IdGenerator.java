package cn.edu.app.douyu.server.common;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ID 生成工具：为用户、帖子、订单等实体生成字符串 ID。
 */
@Component
public class IdGenerator {
    public String next(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
