package cn.edu.app.douyu.server.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI douyuOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("豆笃 Doyu API")
                        .version("v1")
                        .description("""
                                第一阶段 Android 联调 API。所有业务接口统一使用 /api/v1 前缀和 Bearer JWT 鉴权。
                                用户侧认证使用邮箱密码注册和登录；注册成功后直接返回登录会话。
                                Stub Provider: OSS 预签名返回占位 uploadUrl；AI 拼豆任务返回占位图纸和材料清单；微信支付、支付宝支付仅返回占位 App 拉起参数并提供回调幂等骨架。
                                当前支付不是正式微信/支付宝支付。正式上线前必须补齐渠道验签、金额校验、订单号校验、回调重放处理、主动查询和对账。
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
