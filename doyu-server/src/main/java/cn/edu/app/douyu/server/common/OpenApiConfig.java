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
                        .title("Doyu API")
                        .version("v1")
                        .description("""
                                Android integration API. Business endpoints use the /api/v1 prefix and Bearer JWT auth.
                                User authentication uses email/password registration and login.
                                Runtime scope keeps users, community, product browsing, cart, basic order records, address snapshots, and OSS upload integration.
                                Removed runtime surfaces are not exposed as API contracts.
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
