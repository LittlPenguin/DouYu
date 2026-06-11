package cn.edu.app.douyu.server.order;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.CartItemEntity;
import cn.edu.app.douyu.server.common.entity.CartItemRepository;
import cn.edu.app.douyu.server.common.entity.IdempotencyRecordEntity;
import cn.edu.app.douyu.server.common.entity.IdempotencyRecordRepository;
import cn.edu.app.douyu.server.common.entity.OrderEntity;
import cn.edu.app.douyu.server.common.entity.OrderItemEntity;
import cn.edu.app.douyu.server.common.entity.OrderItemRepository;
import cn.edu.app.douyu.server.common.entity.OrderRepository;
import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.SkuEntity;
import cn.edu.app.douyu.server.common.entity.SkuRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单接口 Controller：创建订单、消费购物车、锁定 SKU 库存并处理幂等请求。
 */
@Tag(name = "Orders", description = "Create orders")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    // 订单、订单项、购物车、SKU 和幂等记录分别由对应 Repository 持久化。
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final SkuRepository skuRepository;
    private final ProductRepository productRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    public OrderController(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                           CartItemRepository cartItemRepository, SkuRepository skuRepository,
                           ProductRepository productRepository, IdempotencyRecordRepository idempotencyRepository,
                           IdGenerator idGenerator, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.skuRepository = skuRepository;
        this.productRepository = productRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.idGenerator = idGenerator;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Create order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid argument"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "SKU or cart item not found"),
            @ApiResponse(responseCode = "409", description = "Unavailable product or inventory")
    })
    @Transactional
    @PostMapping
    // 创建订单：支持购物车 itemIds 和立即购买 items，并用 Idempotency-Key 防重复下单。
    Map<String, Object> create(Authentication authentication,
                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                               @Valid @RequestBody CreateOrderRequest request) throws JsonProcessingException {
        String userId = CurrentUser.userId(authentication);
        var existing = idempotencyRepository.findByUserIdAndIdempotencyKeyAndOperation(userId, idempotencyKey, "ORDER");
        if (existing.isPresent()) {
            // 同一用户同一幂等 key 的重复请求直接返回首次响应。
            return readMap(existing.get().getResponseBody());
        }

        List<ResolvedOrderLine> lines = resolveLines(userId, request);
        if (lines.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Order must contain at least one item");
        }

        Map<String, Object> addressSnapshot = resolveAddressSnapshot(request);
        Instant now = Instant.now();
        int total = 0;
        for (ResolvedOrderLine line : lines) {
            // 下单前逐行校验商品可买，并锁定库存。
            requirePurchasableSku(line.sku(), line.product());
            lockStock(line.sku(), line.quantity());
            total += line.sku().getPriceCent() * line.quantity();
        }

        OrderEntity order = new OrderEntity();
        order.setId(idGenerator.next("ord"));
        order.setBuyerId(userId);
        order.setSellerType("SELF_OPERATED");
        order.setOrderType("SELF_OPERATED");
        order.setStatus("CREATED");
        order.setTotalAmountCent(total);
        order.setPayableAmountCent(total);
        order.setAddressSnapshot(objectMapper.writeValueAsString(addressSnapshot));
        order.setExpiresAt(now.plusSeconds(900));
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderRepository.save(order);

        for (ResolvedOrderLine line : lines) {
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setId(idGenerator.next("oi"));
            orderItem.setOrderId(order.getId());
            orderItem.setSkuId(line.sku().getId());
            orderItem.setProductId(line.sku().getProductId());
            orderItem.setQuantity(line.quantity());
            orderItem.setPriceCent(line.sku().getPriceCent());
            orderItem.setCreatedAt(now);
            orderItem.setUpdatedAt(now);
            orderItemRepository.save(orderItem);
            if (line.cartItemId() != null) {
                cartItemRepository.deleteById(line.cartItemId());
            }
        }

        Map<String, Object> response = orderView(order);
        Instant idemNow = Instant.now();
        idempotencyRepository.save(new IdempotencyRecordEntity(
                idGenerator.next("idem"), userId, idempotencyKey, "ORDER", "",
                objectMapper.writeValueAsString(response), idemNow, idemNow));
        return response;
    }

    // 把购物车 itemIds 或立即购买 items 解析成统一的订单行。
    private List<ResolvedOrderLine> resolveLines(String userId, CreateOrderRequest request) {
        List<ResolvedOrderLine> lines = new ArrayList<>();
        if (request.itemIds() != null) {
            for (String itemId : request.itemIds()) {
                if (isBlank(itemId)) {
                    throw new BizException(ErrorCode.INVALID_ARGUMENT, "Cart item id is required");
                }
                CartItemEntity cartItem = cartItemRepository.findById(itemId).orElse(null);
                if (cartItem == null || !cartItem.getUserId().equals(userId)) {
                    throw new BizException(ErrorCode.NOT_FOUND, "Cart item not found");
                }
                SkuEntity sku = requireSku(cartItem.getSkuId());
                ProductEntity product = productRepository.findById(sku.getProductId()).orElse(null);
                lines.add(new ResolvedOrderLine(sku, product, cartItem.getQuantity(), cartItem.getId()));
            }
        }
        if (request.items() != null) {
            for (OrderLineRequest item : request.items()) {
                SkuEntity sku = requireSku(item.skuId());
                ProductEntity product = productRepository.findById(sku.getProductId()).orElse(null);
                lines.add(new ResolvedOrderLine(sku, product, item.quantity(), null));
            }
        }
        return lines;
    }

    // 地址快照随订单保存，后续用户修改地址不会影响历史订单。
    private Map<String, Object> resolveAddressSnapshot(CreateOrderRequest request) {
        if (request.addressSnapshot() != null && !request.addressSnapshot().isEmpty()) {
            Map<String, Object> snapshot = new LinkedHashMap<>(request.addressSnapshot());
            requireAddressField(snapshot, "recipient");
            requireAddressField(snapshot, "phone");
            requireAddressField(snapshot, "region");
            requireAddressField(snapshot, "detail");
            return snapshot;
        }
        throw new BizException(ErrorCode.INVALID_ARGUMENT, "Address snapshot is required");
    }

    // 地址必填字段统一 trim 并校验非空。
    private void requireAddressField(Map<String, Object> snapshot, String fieldName) {
        Object value = snapshot.get(fieldName);
        if (!(value instanceof String text) || text.trim().isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Address " + fieldName + " is required");
        }
        snapshot.put(fieldName, text.trim());
    }

    // SKU 是订单行的购买单位，找不到时返回 404。
    private SkuEntity requireSku(String skuId) {
        return skuRepository.findById(skuId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "SKU not found"));
    }

    // 订单当前只允许购买自营、上架、审核通过且 SKU 在售的商品。
    private void requirePurchasableSku(SkuEntity sku, ProductEntity product) {
        if (product == null || !"ON_SALE".equals(product.getStatus()) || !"PASS".equals(product.getAuditStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "Product is unavailable");
        }
        if (!"SELF_OPERATED".equals(product.getType())) {
            throw new BizException(ErrorCode.CONFLICT, "Player products do not support standard orders");
        }
        if (!"ON_SALE".equals(sku.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "SKU not found");
        }
    }

    // 锁库存使用 synchronized 保护本进程内并发，写入 lockedStock 后保存 SKU。
    private synchronized void lockStock(SkuEntity sku, int quantity) {
        if (sku.getAvailableStock() < quantity) {
            throw new BizException(ErrorCode.INVENTORY_NOT_ENOUGH, "Inventory is not enough");
        }
        sku.setLockedStock(sku.getLockedStock() + quantity);
        sku.setUpdatedAt(Instant.now());
        skuRepository.save(sku);
    }

    // 订单视图：返回金额、地址快照和订单项，供 Android 下单结果页展示。
    private Map<String, Object> orderView(OrderEntity order) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("orderId", order.getId());
        view.put("buyerId", order.getBuyerId());
        view.put("sellerType", order.getSellerType());
        view.put("sellerId", order.getSellerId());
        view.put("orderType", order.getOrderType());
        view.put("status", order.getStatus());
        view.put("totalAmountCent", order.getTotalAmountCent());
        view.put("payableAmountCent", order.getPayableAmountCent());
        view.put("addressSnapshot", order.getAddressSnapshot() != null ? fromJson(order.getAddressSnapshot()) : Map.of());
        view.put("items", orderItemRepository.findByOrderId(order.getId()).stream().map(item -> {
            SkuEntity sku = skuRepository.findById(item.getSkuId()).orElse(null);
            ProductEntity product = productRepository.findById(item.getProductId()).orElse(null);
            Map<String, Object> itemView = new LinkedHashMap<>();
            itemView.put("orderItemId", item.getId());
            itemView.put("skuId", item.getSkuId());
            itemView.put("productId", item.getProductId());
            itemView.put("title", product != null ? product.getTitle() : "");
            itemView.put("specName", sku != null ? sku.getSpecName() : "");
            itemView.put("quantity", item.getQuantity());
            itemView.put("priceCent", item.getPriceCent());
            itemView.put("rowAmountCent", item.getPriceCent() * item.getQuantity());
            return itemView;
        }).toList());
        return view;
    }

    // 地址快照从 JSON 还原成 Map，解析失败时保留 raw 内容便于排查。
    private Map<String, Object> fromJson(String json) {
        if (json == null) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructMapType(LinkedHashMap.class, String.class, Object.class));
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }

    // 幂等记录里保存的是首次响应 JSON，重复请求时反序列化返回。
    private Map<String, Object> readMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory()
                .constructMapType(LinkedHashMap.class, String.class, Object.class));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateOrderRequest(
            List<@NotBlank String> itemIds,
            List<@Valid OrderLineRequest> items,
            Map<String, Object> addressSnapshot,
            String remark
    ) {
    }

    public record OrderLineRequest(@NotBlank String skuId, @Positive int quantity) {
    }

    private record ResolvedOrderLine(SkuEntity sku, ProductEntity product, int quantity, String cartItemId) {
    }
}
