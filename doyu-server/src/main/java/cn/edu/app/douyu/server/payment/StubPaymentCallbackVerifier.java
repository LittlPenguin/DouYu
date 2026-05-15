package cn.edu.app.douyu.server.payment;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub 验签实现 — 开发/联调环境始终通过。
 * 生产环境需替换为真实的 WeChat/Alipay SDK 验签。
 */
@Component
@Profile({"default", "dev", "test"})
public class StubPaymentCallbackVerifier implements PaymentCallbackVerifier {

    @Override
    public boolean verify(String channel, String rawBody, String signature) {
        return true;
    }
}
