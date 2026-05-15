package cn.edu.app.douyu.server.payment;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Order;
import cn.edu.app.douyu.server.common.Models.Payment;
import cn.edu.app.douyu.server.common.Models.Refund;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "支付", description = "支付单创建、支付回调、退款")
@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private static final Set<String> CHANNELS = Set.of("WECHAT_APP", "ALIPAY_APP");
    private final InMemoryStore store;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public PaymentController(InMemoryStore store, IdGenerator idGenerator, ObjectMapper objectMapper) {
        this.store = store;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "创建支付单", description = "为已创建的订单创建支付单，需要 Idempotency-Key 头")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "支付渠道不支持"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权支付该订单"),
            @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @PostMapping("/payments")
    Map<String, Object> createPayment(Authentication authentication,
                                      @RequestHeader("Idempotency-Key") String idempotencyKey,
                                      @Valid @RequestBody CreatePaymentRequest request) throws JsonProcessingException {
        String userId = CurrentUser.userId(authentication);
        Order order = requireOrder(request.orderId());
        if (!order.buyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权支付该订单");
        }
        if (!CHANNELS.contains(request.channel())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "支付渠道不支持");
        }
        String key = "PAYMENT:" + userId + ":" + idempotencyKey;
        if (store.idempotencyResponses.containsKey(key)) {
            return readMap(store.idempotencyResponses.get(key));
        }
        Payment payment = new Payment(idGenerator.next("pay"), order.id(), request.channel(), "CREATED",
                order.payableAmountCent(), null, null);
        store.payments.put(payment.id(), payment);
        Map<String, Object> response = paymentView(payment);
        store.idempotencyResponses.put(key, objectMapper.writeValueAsString(response));
        return response;
    }

    @Operation(summary = "查询支付状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权查看该支付单"),
            @ApiResponse(responseCode = "404", description = "支付单不存在")
    })
    @GetMapping("/payments/{paymentId}")
    Map<String, Object> payment(Authentication authentication, @PathVariable String paymentId) {
        String userId = CurrentUser.userId(authentication);
        Payment payment = requirePayment(paymentId);
        Order order = requireOrder(payment.orderId());
        if (!order.buyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该支付单");
        }
        return paymentView(payment);
    }

    @Operation(summary = "微信支付回调", description = "微信支付异步通知回调（公开接口）")
    @ApiResponse(responseCode = "200", description = "处理成功")
    @PostMapping("/payments/callbacks/wechat")
    Map<String, Object> wechatCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return callback(request);
    }

    @Operation(summary = "支付宝支付回调", description = "支付宝异步通知回调（公开接口）")
    @ApiResponse(responseCode = "200", description = "处理成功")
    @PostMapping("/payments/callbacks/alipay")
    Map<String, Object> alipayCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return callback(request);
    }

    @Operation(summary = "申请退款", description = "为已支付的订单申请退款，需要 Idempotency-Key 头")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "退款成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权退款该订单"),
            @ApiResponse(responseCode = "404", description = "订单或支付单不存在"),
            @ApiResponse(responseCode = "409", description = "支付未成功或退款金额超限")
    })
    @PostMapping("/refunds")
    Map<String, Object> refund(Authentication authentication,
                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                               @Valid @RequestBody RefundRequest request) throws JsonProcessingException {
        String userId = CurrentUser.userId(authentication);
        Order order = requireOrder(request.orderId());
        if (!order.buyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权退款该订单");
        }
        Payment payment = requirePayment(request.paymentId());
        if (!"SUCCEEDED".equals(payment.status())) {
            throw new BizException(ErrorCode.CONFLICT, "支付未成功不可退款");
        }
        if (request.amountCent() > payment.amountCent()) {
            throw new BizException(ErrorCode.CONFLICT, "退款金额超过可退金额");
        }
        String key = "REFUND:" + userId + ":" + idempotencyKey;
        if (store.idempotencyResponses.containsKey(key)) {
            return readMap(store.idempotencyResponses.get(key));
        }
        Refund refund = new Refund(idGenerator.next("rf"), order.id(), payment.id(), request.amountCent(), request.reason(), "CREATED");
        store.refunds.put(refund.id(), refund);
        Map<String, Object> response = Map.of("refundId", refund.id(), "status", refund.status(), "amountCent", refund.amountCent());
        store.idempotencyResponses.put(key, objectMapper.writeValueAsString(response));
        return response;
    }

    private Map<String, Object> callback(PaymentCallbackRequest request) {
        Payment payment = requirePayment(request.paymentId());
        if (payment.channelTradeNo() != null && payment.channelTradeNo().equals(request.channelTradeNo())) {
            return paymentView(payment);
        }
        if (!request.paid()) {
            Payment failed = new Payment(payment.id(), payment.orderId(), payment.channel(), "FAILED", payment.amountCent(), request.channelTradeNo(), null);
            store.payments.put(payment.id(), failed);
            return paymentView(failed);
        }
        Payment succeeded = new Payment(payment.id(), payment.orderId(), payment.channel(), "SUCCEEDED", payment.amountCent(), request.channelTradeNo(), Instant.now());
        store.payments.put(payment.id(), succeeded);
        Order order = requireOrder(payment.orderId());
        store.orders.put(order.id(), new Order(order.id(), order.buyerId(), order.sellerType(), order.sellerId(), order.orderType(),
                "PAID", order.totalAmountCent(), order.payableAmountCent(), order.address(), order.expiresAt(), order.createdAt()));
        List<cn.edu.app.douyu.server.common.Models.OrderItem> items = store.orderItems.getOrDefault(order.id(), List.of());
        items.forEach(item -> store.deductLockedStock(item.skuId(), item.quantity()));
        return paymentView(succeeded);
    }

    private Map<String, Object> paymentView(Payment payment) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("paymentId", payment.id());
        view.put("orderId", payment.orderId());
        view.put("channel", payment.channel());
        view.put("status", payment.status());
        view.put("amountCent", payment.amountCent());
        view.put("payParams", Map.of("provider", "STUB", "payload", "stub-pay-payload-" + payment.id()));
        view.put("channelTradeNo", payment.channelTradeNo() == null ? "" : payment.channelTradeNo());
        view.put("paidAt", payment.paidAt() != null ? payment.paidAt().toString() : null);
        return view;
    }

    private Order requireOrder(String orderId) {
        Order order = store.orders.get(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private Payment requirePayment(String paymentId) {
        Payment payment = store.payments.get(paymentId);
        if (payment == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "支付单不存在");
        }
        return payment;
    }

    private Map<String, Object> readMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class));
    }

    public record CreatePaymentRequest(@NotBlank String orderId, @NotBlank String channel) {
    }

    public record PaymentCallbackRequest(@NotBlank String paymentId, @NotBlank String channelTradeNo, boolean paid) {
    }

    public record RefundRequest(@NotBlank String orderId, @NotBlank String paymentId, @Positive int amountCent, @NotBlank String reason) {
    }
}
