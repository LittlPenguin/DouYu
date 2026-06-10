package cn.edu.app.douyu.server.upload.oss;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AliyunOssProviderTest {

    @Test
    void presignCreatesPutUrlAndRequiredUploadHeaders() {
        AliyunOssProvider provider = new AliyunOssProvider(
                "https://oss-cn-hangzhou.aliyuncs.com",
                "cn-hangzhou",
                "douyu-test-bucket",
                "test-access-key-id",
                "test-access-key-secret",
                "https://img.example.com/douyu"
        );

        OssProvider.PresignResult result = provider.presign("assets/post_image/file_1/test.png", "image/png", 900);

        assertThat(result.uploadUrl()).contains("assets/post_image/file_1/test.png");
        assertThat(result.uploadUrl()).contains("x-oss-signature");
        assertThat(result.headers()).containsEntry("Content-Type", "image/png");

        provider.destroy();
    }

    @Test
    void publicUrlUsesConfiguredPublicBaseUrlAndEncodesSegments() {
        AliyunOssProvider provider = new AliyunOssProvider(
                "https://oss-cn-hangzhou.aliyuncs.com",
                "cn-hangzhou",
                "douyu-test-bucket",
                "test-access-key-id",
                "test-access-key-secret",
                "https://img.example.com/douyu/"
        );

        assertThat(provider.getPublicUrl("assets/post_image/file 1/test image.png"))
                .isEqualTo("https://img.example.com/douyu/assets/post_image/file%201/test%20image.png");

        provider.destroy();
    }
}
