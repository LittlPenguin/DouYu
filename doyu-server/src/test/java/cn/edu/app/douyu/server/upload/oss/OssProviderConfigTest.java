package cn.edu.app.douyu.server.upload.oss;

import cn.edu.app.douyu.server.common.DouyuProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OssProviderConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(TestPropertiesConfig.class, OssProviderConfig.class)
            .withPropertyValues(
                    "douyu.storage.local-path=./target/doyu-test-storage",
                    "douyu.storage.base-url=http://localhost:8081"
            );

    @Test
    void missingProviderUsesLocalProvider() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OssProvider.class);
            assertThat(context.getBean(OssProvider.class)).isInstanceOf(LocalOssProvider.class);
        });
    }

    @Test
    void stubProviderCanBeSelectedForTests() {
        contextRunner
                .withPropertyValues("douyu.oss.provider=stub")
                .run(context -> {
                    assertThat(context).hasSingleBean(OssProvider.class);
                    assertThat(context.getBean(OssProvider.class)).isInstanceOf(StubOssProvider.class);
                });
    }

    @Test
    void aliyunProviderCanBeSelectedWithRequiredSettings() {
        contextRunner
                .withPropertyValues(
                        "douyu.oss.provider=aliyun",
                        "douyu.oss.aliyun.endpoint=https://oss-cn-hangzhou.aliyuncs.com",
                        "douyu.oss.aliyun.region=cn-hangzhou",
                        "douyu.oss.aliyun.bucket=douyu-test-bucket",
                        "douyu.oss.aliyun.access-key-id=test-access-key-id",
                        "douyu.oss.aliyun.access-key-secret=test-access-key-secret",
                        "douyu.oss.aliyun.public-base-url=https://img.example.com/douyu"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OssProvider.class);
                    assertThat(context.getBean(OssProvider.class)).isInstanceOf(AliyunOssProvider.class);
                });
    }

    @Configuration
    @EnableConfigurationProperties(DouyuProperties.class)
    static class TestPropertiesConfig {
    }
}
