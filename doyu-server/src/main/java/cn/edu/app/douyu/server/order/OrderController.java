package cn.edu.app.douyu.server.order;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.PageResult;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Orders", description = "Create, list, inspect, and cancel orders")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
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
    Map<String, Object> create(Authentication authentication,
                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                               @Valid @RequestBody CreateOrderRequest request) throws JsonProcessingException {
        String userId = CurrentUser.userId(authentication);
        var existing = idempotencyRepository.findByUserIdAndIdempotencyKeyAndOperation(userId, idempotencyKey, "ORDER");
        if (existing.isPresent()) {
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

    @Operation(summary = "Order list")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    PageResult<Map<String, Object>> orders(Authentication authentication,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::orderView)
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "Order detail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    Map<String, Object> order(Authentication authentication, @PathVariable String orderId) {
        String userId = CurrentUser.userId(authentication);
        OrderEntity order = requireOrder(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "Cannot view this order");
        }
        return orderView(order);
    }

    @Operation(summary = "Cancel order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Canceled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order cannot be canceled")
    })
    @Transactional
    @PostMapping("/{orderId}/cancel")
    Map<String, Object> cancel(Authentication authentication, @PathVariable String orderId) {
        String userId = CurrentUser.userId(authentication);
        OrderEntity order = requireOrder(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "Cannot cancel this order");
        }
        if (!"CREATED".equals(order.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "Order cannot be canceled in this status");
        }

        for (OrderItemEntity item : orderItemRepository.findByOrderId(order.getId())) {
            SkuEntity sku = skuRepository.findById(item.getSkuId()).orElse(null);
            if (sku != null) {
                sku.setLockedStock(Math.max(0, sku.getLockedStock() - item.getQuantity()));
                sku.setUpdatedAt(Instant.now());
                skuRepository.save(sku);
            }
        }
        order.setStatus("CANCELED");
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        return orderView(order);
    }

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

    private Map<String, Object> resolveAddressSnapshot(CreateOrderRequest request) {
        if (request.addressSnapshot() != null && !request.addressSnapshot().isEmpty()) {
            Map<String, Object> snapshot = new LinkedHashMap<>(request.addressSnapshot());
            requireAddressField(snapshot, "recipient");
            requireAddressField(snapshot, "phone");
            requireAddressField(snapshot, "region");
            requireAddressField(snapshot, "detail");
            return snapshot;
        }
        if (!isBlank(request.addressId())) {
            return Map.of("addressId", request.addressId().trim());
        }
        throw new BizException(ErrorCode.INVALID_ARGUMENT, "Address snapshot is required");
    }

    private void requireAddressField(Map<String, Object> snapshot, String fieldName) {
        Object value = snapshot.get(fieldName);
        if (!(value instanceof String text) || text.trim().isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "Address " + fieldName + " is required");
        }
        snapshot.put(fieldName, text.trim());
    }

    private SkuEntity requireSku(String skuId) {
        return skuRepository.findById(skuId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "SKU not found"));
    }

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

    private synchronized void lockStock(SkuEntity sku, int quantity) {
        if (sku.getAvailableStock() < quantity) {
            throw new BizException(ErrorCode.INVENTORY_NOT_ENOUGH, "Inventory is not enough");
        }
        sku.setLockedStock(sku.getLockedStock() + quantity);
        sku.setUpdatedAt(Instant.now());
        skuRepository.save(sku);
    }

    private OrderEntity requireOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Order not found"));
    }

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

    private Map<String, Object> readMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory()
                .constructMapType(LinkedHashMap.class, String.class, Object.class));
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateOrderRequest(
            List<@NotBlank String> itemIds,
            List<@Valid OrderLineRequest> items,
            String addressId,
            Map<String, Object> addressSnapshot,
            String remark
    ) {
    }

    public record OrderLineRequest(@NotBlank String skuId, @Positive int quantity) {
    }

    private record ResolvedOrderLine(SkuEntity sku, ProductEntity product, int quantity, String cartItemId) {
    }
}
