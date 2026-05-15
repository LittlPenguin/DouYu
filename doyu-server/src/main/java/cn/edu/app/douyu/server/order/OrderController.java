package cn.edu.app.douyu.server.order;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
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

@Tag(name = "订单", description = "创建订单、订单列表、取消订单")
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

    @Operation(summary = "创建订单")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "SKU 不存在"),
            @ApiResponse(responseCode = "409", description = "库存不足或玩家商品不支持标准订单")
    })
    @PostMapping
    Map<String, Object> create(Authentication authentication,
                               @RequestHeader("Idempotency-Key") String idempotencyKey,
                               @Valid @RequestBody CreateOrderRequest request) throws JsonProcessingException {
        String userId = CurrentUser.userId(authentication);
        String key = "ORDER:" + userId + ":" + idempotencyKey;
        var existing = idempotencyRepository.findByUserIdAndIdempotencyKeyAndOperation(userId, idempotencyKey, "ORDER");
        if (existing.isPresent()) {
            return readMap(existing.get().getResponseBody());
        }
        Instant now = Instant.now();
        int total = 0;
        List<CartItemEntity> resolvedItems = new ArrayList<>();
        for (String itemId : request.itemIds()) {
            CartItemEntity cartItem = cartItemRepository.findById(itemId).orElse(null);
            if (cartItem == null || !cartItem.getUserId().equals(userId)) {
                throw new BizException(ErrorCode.NOT_FOUND, "购物车项不存在");
            }
            SkuEntity sku = skuRepository.findById(cartItem.getSkuId())
                    .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "SKU 不存在"));
            ProductEntity product = productRepository.findById(sku.getProductId()).orElse(null);
            if (product != null && !"SELF_OPERATED".equals(product.getType())) {
                throw new BizException(ErrorCode.CONFLICT, "玩家商品不支持标准订单");
            }
            lockStock(sku, cartItem.getQuantity());
            total += sku.getPriceCent() * cartItem.getQuantity();
            resolvedItems.add(cartItem);
        }
        OrderEntity order = new OrderEntity();
        order.setId(idGenerator.next("ord"));
        order.setBuyerId(userId);
        order.setSellerType("SELF_OPERATED");
        order.setOrderType("SELF_OPERATED");
        order.setStatus("WAITING_PAYMENT");
        order.setTotalAmountCent(total);
        order.setPayableAmountCent(total);
        order.setAddressSnapshot("{\"addressId\":\"" + request.addressId() + "\"}");
        order.setExpiresAt(now.plusSeconds(900));
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderRepository.save(order);
        for (CartItemEntity cartItem : resolvedItems) {
            SkuEntity sku = skuRepository.findById(cartItem.getSkuId()).orElseThrow();
            OrderItemEntity oi = new OrderItemEntity();
            oi.setId(idGenerator.next("oi"));
            oi.setOrderId(order.getId());
            oi.setSkuId(sku.getId());
            oi.setProductId(sku.getProductId());
            oi.setQuantity(cartItem.getQuantity());
            oi.setPriceCent(sku.getPriceCent());
            oi.setCreatedAt(now);
            oi.setUpdatedAt(now);
            orderItemRepository.save(oi);
            cartItemRepository.deleteById(cartItem.getId());
        }
        Map<String, Object> response = orderView(order);
        Instant now2 = Instant.now();
        idempotencyRepository.save(new IdempotencyRecordEntity(
                idGenerator.next("idem"), userId, idempotencyKey, "ORDER", "",
                objectMapper.writeValueAsString(response), now2, now2));
        return response;
    }

    @Operation(summary = "订单列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping
    PageResult<Map<String, Object>> orders(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<Map<String, Object>> items = orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::orderView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "订单详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权查看该订单"),
            @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    @GetMapping("/{orderId}")
    Map<String, Object> order(Authentication authentication, @PathVariable String orderId) {
        String userId = CurrentUser.userId(authentication);
        OrderEntity order = requireOrder(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该订单");
        }
        return orderView(order);
    }

    @Operation(summary = "取消订单")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "无权取消该订单"),
            @ApiResponse(responseCode = "404", description = "订单不存在"),
            @ApiResponse(responseCode = "409", description = "当前订单状态不可取消")
    })
    @PostMapping("/{orderId}/cancel")
    Map<String, Object> cancel(Authentication authentication, @PathVariable String orderId) {
        String userId = CurrentUser.userId(authentication);
        OrderEntity order = requireOrder(orderId);
        if (!order.getBuyerId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权取消该订单");
        }
        if (!"WAITING_PAYMENT".equals(order.getStatus()) && !"CREATED".equals(order.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "当前订单状态不可取消");
        }
        order.setStatus("CANCELED");
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        return orderView(order);
    }

    private synchronized void lockStock(SkuEntity sku, int quantity) {
        if (!"ON_SALE".equals(sku.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "SKU 不存在");
        }
        if (sku.getAvailableStock() < quantity) {
            throw new BizException(ErrorCode.INVENTORY_NOT_ENOUGH, "库存不足");
        }
        sku.setLockedStock(sku.getLockedStock() + quantity);
        sku.setUpdatedAt(Instant.now());
        skuRepository.save(sku);
    }

    private OrderEntity requireOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "订单不存在"));
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
            return itemView;
        }).toList());
        return view;
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null) return Map.of();
        try { return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class)); }
        catch (Exception e) { return Map.of("raw", json); }
    }

    private Map<String, Object> readMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class));
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record CreateOrderRequest(List<@NotBlank String> itemIds, @NotBlank String addressId, String remark) {
    }
}
