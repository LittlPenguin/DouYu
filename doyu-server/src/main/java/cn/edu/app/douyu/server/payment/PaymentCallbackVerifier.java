package cn.edu.app.douyu.server.payment;

/**
 * 支付回调验签接口。
 * 当前实现：StubPaymentCallbackVerifier（测试/联调用，始终通过）。
 * 后续实现：WeChatCallbackVerifier、AlipayCallbackVerifier。
 */
public interface PaymentCallbackVerifier {

    /**
     * 验证回调签名是否合法。
     *
     * @param channel   支付渠道（WECHAT_APP / ALIPAY_APP）
     * @param rawBody   原始请求体
     * @param signature 签名值（来自请求头或参数）
     * @return 验签结果
     */
    boolean verify(String channel, String rawBody, String signature);
}
