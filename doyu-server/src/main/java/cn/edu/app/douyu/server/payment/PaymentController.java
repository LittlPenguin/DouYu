package cn.edu.app.douyu.server.payment;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.*;
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

import java.time.Duration;
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
    private static final Duration CALLBACK_MAX_AGE = Duration.ofMinutes(10);
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SkuRepository skuRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final PaymentCallbackVerifier callbackVerifier;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public PaymentController(PaymentRepository paymentRepository, OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository, SkuRepository skuRepository,
                             IdempotencyRecordRepository idempotencyRepository,
                             PaymentCallbackVerifier callbackVerifier,
                             IdGenerator idGenerator, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.skuRepository = skuRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.callbackVerifier = callbackVerifier;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "创建支付单")
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
        OrderEntity order = requireOrder(request.orderId());
        if (!order.getBuyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权支付该订单");
        }
        if (!CHANNELS.contains(request.channel())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "支付渠道不支持");
        }
        String key = "PAYMENT:" + userId + ":" + idempotencyKey;
        var existing = idempotencyRepository.findByUserIdAndIdempotencyKeyAndOperation(userId, idempotencyKey, "PAYMENT");
        if (existing.isPresent()) {
            return readMap(existing.get().getResponseBody());
        }
        Instant now = Instant.now();
        PaymentEntity payment = new PaymentEntity();
        payment.setId(idGenerator.next("pay"));
        payment.setOrderId(order.getId());
        payment.setChannel(request.channel());
        payment.setStatus("CREATED");
        payment.setAmountCent(order.getPayableAmountCent());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        paymentRepository.save(payment);
        Map<String, Object> response = paymentView(payment);
        Instant now2 = Instant.now();
        idempotencyRepository.save(new IdempotencyRecordEntity(
                idGenerator.next("idem"), userId, idempotencyKey, "PAYMENT", "",
                objectMapper.writeValueAsString(response), now2, now2));
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
        PaymentEntity payment = requirePayment(paymentId);
        OrderEntity order = requireOrder(payment.getOrderId());
        if (!order.getBuyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该支付单");
        }
        return paymentView(payment);
    }

    @Operation(summary = "微信支付回调")
    @ApiResponse(responseCode = "200", description = "处理成功")
    @PostMapping("/payments/callbacks/wechat")
    Map<String, Object> wechatCallback(@RequestHeader(value = "X-Signature", required = false) String signature,
                                       @Valid @RequestBody PaymentCallbackRequest request) {
        return callback("WECHAT_APP", signature, request);
    }

    @Operation(summary = "支付宝支付回调")
    @ApiResponse(responseCode = "200", description = "处理成功")
    @PostMapping("/payments/callbacks/alipay")
    Map<String, Object> alipayCallback(@RequestHeader(value = "X-Signature", required = false) String signature,
                                       @Valid @RequestBody PaymentCallbackRequest request) {
        return callback("ALIPAY_APP", signature, request);
    }

    @Operation(summary = "申请退款")
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
        OrderEntity order = requireOrder(request.orderId());
        if (!order.getBuyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权退款该订单");
        }
        PaymentEntity payment = requirePayment(request.paymentId());
        if (!"SUCCEEDED".equals(payment.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "支付未成功不可退款");
        }
        if (request.amountCent() > payment.getAmountCent()) {
            throw new BizException(ErrorCode.CONFLICT, "退款金额超过可退金额");
        }
        String key = "REFUND:" + userId + ":" + idempotencyKey;
        var existing = idempotencyRepository.findByUserIdAndIdempotencyKeyAndOperation(userId, idempotencyKey, "REFUND");
        if (existing.isPresent()) {
            return readMap(existing.get().getResponseBody());
        }
        Map<String, Object> response = Map.of("refundId", idGenerator.next("rf"), "status", "CREATED", "amountCent", request.amountCent());
        Instant now2 = Instant.now();
        idempotencyRepository.save(new IdempotencyRecordEntity(
                idGenerator.next("idem"), userId, idempotencyKey, "REFUND", "",
                objectMapper.writeValueAsString(response), now2, now2));
        return response;
    }

    private Map<String, Object> callback(String channel, String signature, PaymentCallbackRequest request) {
        // 1. 验签
        if (!callbackVerifier.verify(channel, request.toString(), signature)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "回调签名验证失败");
        }
        PaymentEntity payment = requirePayment(request.paymentId());
        // 2. 幂等：同一 channelTradeNo 不重复处理
        if (payment.getChannelTradeNo() != null && payment.getChannelTradeNo().equals(request.channelTradeNo())) {
            return paymentView(payment);
        }
        // 3. 渠道一致性校验
        if (!channel.equals(payment.getChannel())) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "回调渠道与支付单不一致");
        }
        // 4. 时间窗口防重放（回调时间与支付单创建时间差不超过 10 分钟）
        Instant now = Instant.now();
        if (request.callbackTime() != null) {
            Duration age = Duration.between(payment.getCreatedAt(), request.callbackTime());
            if (age.isNegative() || age.compareTo(CALLBACK_MAX_AGE) > 0) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT, "回调时间超出有效窗口");
            }
        }
        // 5. 处理失败回调
        if (!request.paid()) {
            payment.setStatus("FAILED");
            payment.setChannelTradeNo(request.channelTradeNo());
            payment.setUpdatedAt(now);
            paymentRepository.save(payment);
            return paymentView(payment);
        }
        // 6. 金额校验：回调金额必须与支付单一致
        if (request.amountCent() != null && request.amountCent() != payment.getAmountCent()) {
            throw new BizException(ErrorCode.CONFLICT, "回调金额与支付单不一致");
        }
        // 7. 成功处理
        payment.setStatus("SUCCEEDED");
        payment.setChannelTradeNo(request.channelTradeNo());
        payment.setPaidAt(now);
        payment.setUpdatedAt(now);
        paymentRepository.save(payment);
        OrderEntity order = requireOrder(payment.getOrderId());
        order.setStatus("PAID");
        order.setUpdatedAt(now);
        orderRepository.save(order);
        // Deduct locked stock
        orderItemRepository.findByOrderId(order.getId()).forEach(item -> {
            SkuEntity sku = skuRepository.findById(item.getSkuId()).orElse(null);
            if (sku != null) {
                sku.setStock(sku.getStock() - item.getQuantity());
                sku.setLockedStock(Math.max(0, sku.getLockedStock() - item.getQuantity()));
                sku.setUpdatedAt(now);
                skuRepository.save(sku);
            }
        });
        return paymentView(payment);
    }

    private Map<String, Object> paymentView(PaymentEntity payment) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("paymentId", payment.getId());
        view.put("orderId", payment.getOrderId());
        view.put("channel", payment.getChannel());
        view.put("status", payment.getStatus());
        view.put("amountCent", payment.getAmountCent());
        view.put("payParams", Map.of("provider", "STUB", "payload", "stub-pay-payload-" + payment.getId()));
        view.put("channelTradeNo", payment.getChannelTradeNo() == null ? "" : payment.getChannelTradeNo());
        view.put("paidAt", payment.getPaidAt() != null ? payment.getPaidAt().toString() : null);
        return view;
    }

    private OrderEntity requireOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "订单不存在"));
    }

    private PaymentEntity requirePayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "支付单不存在"));
    }

    private Map<String, Object> readMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class));
    }

    public record CreatePaymentRequest(@NotBlank String orderId, @NotBlank String channel) {
    }

    public record PaymentCallbackRequest(@NotBlank String paymentId, @NotBlank String channelTradeNo,
                                         boolean paid, Integer amountCent, Instant callbackTime) {
    }

    public record RefundRequest(@NotBlank String orderId, @NotBlank String paymentId, @Positive int amountCent, @NotBlank String reason) {
    }
}
